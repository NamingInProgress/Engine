package com.vke;

import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.core.logger.*;

import java.io.IOException;

public class Main {

    public static final CoreLogger LOG = LoggerFactory.get("VkEngine");

    public static void main(String[] args) throws InterruptedException, ConfigParser.ConfigParseException, IOException, SchemaMismatchException {
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
        //EngineCreateInfo createInfo = new EngineCreateInfo();
        //createInfo.releaseMode = true;
        //createInfo.windowCreateInfo = new WindowCreateInfo("My Window");
//
        //RenderPipelines.init();
        //VKEngine engine = new VKEngine(createInfo);

        //VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);

        //engine.start(new TestApp());
    }

}