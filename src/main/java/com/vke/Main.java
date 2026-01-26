package com.vke;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntCharHashMap;
import com.carrotsearch.hppc.ShortObjectMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.vke.api.game.Game;
import com.vke.api.game.Version;
import com.vke.api.parsing.SourceCode;
import com.vke.api.parsing.Tokenizer;
import com.vke.api.serializer.Serializer;
import com.vke.api.vkz.*;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.logger.*;
import com.vke.core.parsing.source.StringSourceCode;
import com.vke.core.parsing.xml.XmlToken;
import com.vke.core.parsing.xml.XmlTokenizer;
import com.vke.core.rendering.vulkan.pipeline.RenderPipelines;
import com.vke.core.vkz.VkzObjLoader;
import com.vke.core.vkz.VkzObjSaver;
import com.vke.core.window.Window;
import com.vke.utils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) throws InterruptedException {
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

        //System.exit(0);
        //Thread.sleep(5000);
        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");


        RenderPipelines.init();
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