package com.vke;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.api.game.Game;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.file.gzip.GzipDecompressor;
import com.vke.core.file.gzip.io.bit.ShittyBitInputStream;
import com.vke.core.logger.*;
import com.vke.core.window.Window;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) {
//        String testXml = "<hello val=\"1\">lmao<hello/>";
//        SourceCode sourceCode = new StringSourceCode(testXml);
//        XmlTokenizer tokenizer = new XmlTokenizer(sourceCode);
//        try {
//            XmlToken tkn = tokenizer.nextToken();
//            while (tkn.getType() != XmlToken.Type.EOF) {
//                System.out.println(tkn);
//                tkn = tokenizer.nextToken();
//                Thread.sleep(1000);
//            }
//        } catch (Tokenizer.TokenizeException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//

//        VkzArchive archive = VkzArchive.open(Main.class.getResourceAsStream("/test.vkz"), OpenStrategy.OpenAllFiles);
//        VkzFileHandle document = archive.file("documents/test.txt");
//        InputStream docStream = document.getInputStream();
//
//        VkzDirectoryHandle docs = archive.directory("documents");
//        for (Iterator<VkzFileHandle> it = docs.iterateFiles(); it.hasNext(); ) {
//            VkzFileHandle doc = it.next();
//            VkzEditor editor = doc.edit();
//            editor.clear();
//            editor.write("Hello world");
//            editor.commit();
//        }
//
//
//        String hello = "Hello world";
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        VkzObjSaver saver = new VkzObjSaver(bos);
//        Serializer.saveObject(hello, saver);
//
//        try {
//            saver.flush();
//            saver.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        byte[] bytes = bos.toByteArray();
//        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        VkzObjLoader loader = new VkzObjLoader(bis, saver.getSavedBytes(), saver.getExtraBits());
//        String hello2 = Serializer.loadObject(String.class, loader);
//        System.out.println(hello2);
//
//
//        String magic = "VKZ0";
//        byte[] b = magic.getBytes(StandardCharsets.US_ASCII);
//        ByteBuffer buffer = ByteBuffer.wrap(b);
//        int magicInt = buffer.getInt();
//        System.out.println(Integer.toHexString(magicInt));
//
//        try {
//            Path c = FileUtils.getConfigFolder("VKEngine");
//            Path p = FileUtils.getCacheFolder("VKEngine");
//            System.out.println("Config: " + c);
//            System.out.println("Cache: " + p);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        String text = "If FTEXT is set, the file is probably ASCII text.  This is\n" +
                "            an optional indication, which the compressor may set by\n" +
                "            checking a small amount of the input data to see whether any\n" +
                "            non-ASCII characters are present.  In case of doubt, FTEXT\n" +
                "            is cleared, indicating binary data. For systems which have\n" +
                "            different file formats for ascii text and binary data, the\n" +
                "            decompressor can use FTEXT to choose the appropriate format.\n" +
                "            We deliberately do not specify the algorithm used to set\n" +
                "            this bit, since a compressor always has the option of\n" +
                "            leaving it cleared and a decompressor always has the option\n" +
                "            of ignoring it and letting some other program handle issues\n" +
                "            of data conversion.";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            gzipOutputStream.write(bytes);
            gzipOutputStream.flush();
            gzipOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] compressed = bos.toByteArray();

        try {
            GzipDecompressor data = new GzipDecompressor(new ShittyBitInputStream(new ByteArrayInputStream(compressed)));
            data.parseHeader();

            ByteArrayList bytes = new ByteArrayList();
            int next;
            while ((next = data.nextByte()) != -1) {
                bytes.add((byte) next);
            }
            String s = new String(bytes.toArray(), StandardCharsets.UTF_8);
            System.out.println(s);

            data.parseFooter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);

        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");



        VKEngine engine = new VKEngine(createInfo);
        engine.start(new Game() {
            @Override
            public void onInit(VKEngine engine) {

            }

            @Override
            public void onDraw(Window window) {

            }
        });
    }

}