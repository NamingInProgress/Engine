package com.vke.core.assets.pipeline.protocols.shader;

import com.vke.core.rendering.spp.SPPLexer;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.utils.Utils;
import com.vke.utils.functionalinterface.TriConsumer;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;
import com.vke.utils.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class ShaderPreprocessor {

    private static ShaderPreprocessor instance;

    public static ShaderPreprocessor getInstance(VulkanRenderer renderer) {
        if (instance == null) instance = new ShaderPreprocessor(renderer);
        return instance;
    }

    private final VulkanRenderer renderer;

    public ShaderPreprocessor(VulkanRenderer renderer) {
        this.renderer = renderer;
    }

    public Pair<String, ShaderMetadata> process(Identifier ident) throws IOException {
        ShaderMetadata meta = new ShaderMetadata(new HashMap<>(), new ArrayList<>(), new HashMap<>());

        String source = processFile(ident, meta, new HashSet<>());

        meta.defaultRuntimeSizes.put("textures", renderer.getBindlessTexturesCount());

        return new Pair<>(source, meta);
    }

    // Small helper class to track modifications uniformly
    private static class Modification {
        final int start;
        final int end;
        final String text;

        Modification(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

    private String processFile(Identifier ident, ShaderMetadata meta, Set<Identifier> includeStack) throws IOException {
        if (!includeStack.add(ident)) {
            throw new IllegalStateException("Circular shader include: " + ident);
        }

        String code = Utils.readStringFromInputStream(ident.asInputStream());

        SPPLexer lexer = new SPPLexer(code.toCharArray());

        ArrayList<SPPLexer.SPPToken> remove = new ArrayList<>();
        ArrayList<Modification> modifications = new ArrayList<>();

        SPPLexer.SPPToken token = lexer.nextToken();

        while (token.type != SPPLexer.TokenType.EOF) {
            if (token.type == SPPLexer.TokenType.HASHTAG) {
                SPPLexer.SPPToken command = lexer.nextToken();
                if (command.type == SPPLexer.TokenType.LITERAL) {
                    if (command.value.equals("include")) {
                        ArrayList<SPPLexer.SPPToken> includeTokens = new ArrayList<>();
                        includeTokens.add(token);    // '#'
                        includeTokens.add(command);  // 'include'

                        Replacement replacement = parseInclude(lexer, includeTokens, meta, includeStack);

                        int includeStart = token.start;
                        int includeEnd = includeTokens.get(includeTokens.size() - 1).end;
                        modifications.add(new Modification(includeStart, includeEnd, replacement.text()));
                    } else {
                        parseCommand(remove, lexer, meta, command, token);
                    }
                }
            }

            token = lexer.nextToken();
        }

        StringBuilder sb = new StringBuilder(code);

        for (SPPLexer.SPPToken t : remove) {
            modifications.add(new Modification(t.start, t.end, ""));
        }

        modifications.sort((m1, m2) -> Integer.compare(m2.start, m1.start));

        for (Modification mod : modifications) {
            sb.replace(mod.start, mod.end, mod.text);
        }

        includeStack.remove(ident);

        return sb.toString();
    }

    public void parseCommand(ArrayList<SPPLexer.SPPToken> remove, SPPLexer lexer, ShaderMetadata meta,
                             SPPLexer.SPPToken command, SPPLexer.SPPToken token) {
        Optional<PreprocessorCommand> cmd = Arrays.stream(PreprocessorCommand.values())
                .filter(c -> c.name.equals(command.value))
                .findFirst();

        if (cmd.isPresent()) {
            consume(remove, token);
            consume(remove, command);

            cmd.get().processFunction.accept(remove, lexer, meta);
        }
    }

    public void parseMultiWrite(ArrayList<SPPLexer.SPPToken> remove, SPPLexer lexer, ShaderMetadata meta) {
        consume(remove, expectAfter(lexer, SPPLexer.TokenType.LPAREN));
        int count = (int) consume(remove, expectAfter(lexer, SPPLexer.TokenType.NUM_LITERAL)).value;
        consume(remove, expectAfter(lexer, SPPLexer.TokenType.RPAREN));
        String name = getStructNameAfterCommand(remove, meta, lexer);
        meta.multipleWrites.put(name, count);
    }

    public void parseStatic(ArrayList<SPPLexer.SPPToken> remove, SPPLexer lexer, ShaderMetadata meta) {
        meta.staticBuffers.add(getStructNameAfterCommand(remove, meta, lexer));
    }

    public void parseDefaultSize(ArrayList<SPPLexer.SPPToken> remove, SPPLexer lexer, ShaderMetadata meta) {
        consume(remove, expectAfter(lexer, SPPLexer.TokenType.LPAREN));
        int count = (int) consume(remove, expectAfter(lexer, SPPLexer.TokenType.NUM_LITERAL)).value;
        consume(remove, expectAfter(lexer, SPPLexer.TokenType.RPAREN));
        expectAfter(lexer, SPPLexer.TokenType.LITERAL);
        String name = (String) expectAfter(lexer, SPPLexer.TokenType.LITERAL).value;
        meta.defaultRuntimeSizes.put(name, count);
    }

    private Replacement parseInclude(SPPLexer lexer, ArrayList<SPPLexer.SPPToken> remove, ShaderMetadata meta, Set<Identifier> includeStack) throws IOException {
        consume(remove, expectAfter(lexer, SPPLexer.TokenType.LPAREN));

        String path = (String) consume(remove, expectAfter(lexer, SPPLexer.TokenType.LITERAL)).value;
        path = path.replace("\"", "");

        SPPLexer.SPPToken close = consume(remove, expectAfter(lexer, SPPLexer.TokenType.RPAREN));

        Identifier included = resolveInclude(path);

        String result = processFile(included, meta, includeStack);

        return new Replacement(close.end, "\n" + result + "\n");
    }

    private Identifier resolveInclude(String path) {
        return Identifier.of(path);
    }

    private SPPLexer.SPPToken consume(ArrayList<SPPLexer.SPPToken> list, SPPLexer.SPPToken token) {
        list.add(token);
        return token;
    }

    public SPPLexer.SPPToken expectAfter(SPPLexer lexer, SPPLexer.TokenType type) {
        SPPLexer.SPPToken next = lexer.nextToken();

        if (next.type != type) {
            throw new IllegalStateException("Expected " + type + " but found " + next.type);
        }

        return next;
    }

    public String getStructNameAfterCommand(ArrayList<SPPLexer.SPPToken> remove, ShaderMetadata meta, SPPLexer lexer) {
        expectAfter(lexer, SPPLexer.TokenType.LITERAL);
        SPPLexer.SPPToken token;
        do {
            token = lexer.nextToken();
        } while (token.type != SPPLexer.TokenType.RPAREN);
        if (expectAfter(lexer, SPPLexer.TokenType.LITERAL).value.equals("readonly")) {
            expectAfter(lexer, SPPLexer.TokenType.LITERAL);
        }
        expectAfter(lexer, SPPLexer.TokenType.LITERAL);
        do {
            token = lexer.nextToken();
            if (token.type == SPPLexer.TokenType.HASHTAG) {
                SPPLexer.SPPToken command = lexer.nextToken();
                parseCommand(remove, lexer, meta, command, token);
            }
        } while (token.type != SPPLexer.TokenType.RBRACE);
        return (String) expectAfter(lexer, SPPLexer.TokenType.LITERAL).value;
    }

    public enum PreprocessorCommand {

        MULTI_WRITE("MultipleWrites", instance::parseMultiWrite),
        STATIC("Static", instance::parseStatic),
        INCLUDE("include", null),
        DEFAULT_SIZE("DefaultSize", instance::parseDefaultSize);


        public final String name;
        public final TriConsumer<ArrayList<SPPLexer.SPPToken>, SPPLexer, ShaderMetadata> processFunction;

        PreprocessorCommand(String name, TriConsumer<ArrayList<SPPLexer.SPPToken>, SPPLexer, ShaderMetadata> processFunction) {
            this.name = name;
            this.processFunction = processFunction;
        }
    }


    public record Replacement(int position, String text) {}

    public record ShaderMetadata(HashMap<String, Integer> multipleWrites, ArrayList<String> staticBuffers,
                                 HashMap<String, Integer> defaultRuntimeSizes) {}
}