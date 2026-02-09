package com.vke.core.parsing.config.xml;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.SourceCursor;
import com.vke.core.parsing.config.xml.nodes.WriteAttribNode;
import com.vke.core.parsing.config.xml.nodes.XmlMetaNode;
import com.vke.core.parsing.config.xml.nodes.XmlTagNode;
import com.vke.core.parsing.config.xml.nodes.XmlValueNode;
import com.vke.core.parsing.config.xml.tokens.XmlToken;
import com.vke.core.parsing.config.xml.tokens.XmlTokenizer;

import java.util.Objects;

public class XmlParser implements ConfigParser {
    private XmlTokenizer tokenizer;

    @Override
    public void setSource(char[] source) {
        tokenizer = new XmlTokenizer(new SourceCursor(source, 0));
    }

    @Override
    public ConfigDocument parse() throws ConfigParseException {
        XmlTagNode root = new XmlTagNode("");
        parseNode(root);
        root.finish();
        return new XmlDocument(root);
    }

    private void parseNode(XmlTagNode parent) throws ConfigParseException {
        try {
            XmlToken next = tokenizer.nextToken();
            if (next.getType() == XmlToken.Type.LTri) {
                tokenizer.setInTagHead(true);
                next = tokenizer.nextToken();
                if (next.getType() == XmlToken.Type.Question) {
                    //meta
                    String tagName = tokenizer.expectToken(XmlToken.Type.Ident).getValue();
                    XmlMetaNode metaNode = new XmlMetaNode(tagName);
                    next = tokenizer.nextToken();
                    if (next.getType() == XmlToken.Type.Ident) {
                        tokenizer.putback(next);
                        parseAttribs(metaNode);
                        next = tokenizer.nextToken();
                    }
                    tokenizer.putback(next);

                    tokenizer.expectToken(XmlToken.Type.Question);
                    tokenizer.expectToken(XmlToken.Type.RTri);

                    parent.addNode(tagName, metaNode);
                    //amazing fix
                    parseNode(parent);
                } else if (next.getType() == XmlToken.Type.Ident){
                    //normal tag
                    tokenizer.setInTagHead(true);
                    String tagName = next.getValue();
                    XmlTagNode tagNode = new XmlTagNode(tagName);

                    next = tokenizer.nextToken();
                    if (next.getType() == XmlToken.Type.Ident) {
                        tokenizer.putback(next);
                        parseAttribs(tagNode);
                        next = tokenizer.nextToken();
                    }

                    if (next.getType() == XmlToken.Type.Slash) {
                        //direct closing tag
                        tokenizer.expectToken(XmlToken.Type.RTri);

                        tagNode.finish();
                        parent.addNode(tagName, tagNode);
                    } else {
                        tokenizer.putback(next);
                        tokenizer.expectToken(XmlToken.Type.RTri);

                        tokenizer.setInTagHead(false);
                        //now do the content. this can either be a new tag or plain content OR the closing tag
                        while (true) {
                            next = tokenizer.nextToken();
                            if (next.getType() == XmlToken.Type.LTri) {
                                //new tag
                                tokenizer.setInTagHead(true);
                                XmlToken peek = tokenizer.nextToken();
                                if (peek.getType() == XmlToken.Type.Slash) {
                                    //closing tag
                                    String closeName = tokenizer.expectToken(XmlToken.Type.Ident).getValue();
                                    if (Objects.equals(closeName, tagName)) {
                                        tokenizer.expectToken(XmlToken.Type.RTri);

                                        tagNode.finish();
                                        parent.addNode(tagName, tagNode);
                                        return;
                                    } else {
                                        throw new ConfigParseException("Closing tag name <" + closeName + "> must match open tag name <" + tagName + ">!");
                                    }
                                } else {
                                    //new tag (child)
                                    tokenizer.putback(next);
                                    tokenizer.putback(peek);
                                    parseNode(tagNode);
                                }
                            } else {
                                //raw content
                                tokenizer.resetToPreviousPosition();
                                String content = tokenizer.collectContent();
                                XmlValueNode valueNode = new XmlValueNode(content.trim());
                                tagNode.addNode(null, valueNode);
                            }
                        }
                    }
                } else {
                    throw new ConfigParseException("Illegal token found");
                }
            } else {
                tokenizer.setInTagHead(false);
                String content = tokenizer.collectContent();
                XmlValueNode valueNode = new XmlValueNode(content);
                parent.addNode(null, valueNode);
            }

        } catch (SourceCursor.EOF eof) {
            throw new ConfigParseException("Unexpected End of input");
        } catch (ConfigParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigParseException(e);
        }
    }

    private void parseAttribs(WriteAttribNode dest) throws SourceCursor.EOF, NumberFormatException, IllegalStateException {
        XmlToken next = tokenizer.nextToken();
        while (next.getType() == XmlToken.Type.Ident) {
            String name = next.getValue();
            tokenizer.expectToken(XmlToken.Type.Eq);
            String value = tokenizer.expectToken(XmlToken.Type.StrLit).getValue();
            dest.addAttrib(name, value);

            next = tokenizer.nextToken();
        }
        tokenizer.putback(next);
    }
}
