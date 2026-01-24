package com.vke;

import com.vke.api.game.Game;
import com.vke.api.vkz.*;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.logger.*;
import com.vke.core.vkz.types.Vkz;
import com.vke.core.window.Window;
import com.vke.utils.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

        Vkz.registerVkzSerializers();

        VkzArchive archive = VkzArchive.createNew();
        VkzDirectoryHandle root = archive.root();
        VkzDirectoryHandle root2 = root.createDirectory("dir");
        VkzDirectoryHandle dir = root2.createDirectory("documents");
        VkzDirectoryHandle dir2 = root2.createDirectory("documents2");
        VkzFileHandle testTextFile = dir.createFile("test.txt");
        VkzFileHandle testTextFile2 = dir2.createFile("test.txt");
        VkzEditor editor = testTextFile.edit();
        editor.write("Hello World!");
        editor.commit();

        VkzEditor editor2 = testTextFile2.edit();
        editor2.write("Hello World!");
        editor2.commit();

        try {
            //Files.createFile(Path.of("a.vkz"));
            FileOutputStream out = new FileOutputStream("a.vkz");
            archive.writeOut(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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