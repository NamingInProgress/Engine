package com.vke;

import com.vke.api.game.Game;
import com.vke.api.game.Version;
import com.vke.api.parsing.SourceCode;
import com.vke.api.parsing.Tokenizer;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.logger.*;
import com.vke.core.parsing.source.StringSourceCode;
import com.vke.core.parsing.xml.XmlToken;
import com.vke.core.parsing.xml.XmlTokenizer;
import com.vke.core.window.Window;

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
//        System.exit(0);


        EngineCreateInfo createInfo = new EngineCreateInfo();
        createInfo.releaseMode = true;
        //createInfo.vulkanCreateInfo.apiVersion = new Version(1, 3, 0);
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