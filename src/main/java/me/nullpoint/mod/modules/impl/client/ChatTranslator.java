package me.nullpoint.mod.modules.impl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.ReceiveMessageEvent;
import me.nullpoint.api.events.impl.SendMessageEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.miscellaneous.ChatSuffix;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringEscapeUtils;

public class ChatTranslator extends Module {
   private final EnumSetting page;
   private final BooleanSetting others;
   private final EnumSetting othersFromChat;
   private final EnumSetting othersToChat;
   private final BooleanSetting yours;
   private final EnumSetting fromMy;
   private final EnumSetting toMy;
   private final GoogleTranslate googleTranslate;

   public ChatTranslator() {
      super("ChatTranslator", Module.Category.Misc);
      this.page = this.add(new EnumSetting("Page", ChatTranslator.Page.Yours));
      this.others = this.add(new BooleanSetting("Others", false, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Others;
      }));
      this.othersFromChat = this.add(new EnumSetting("FromChat", ChatTranslator.FromLanguage.ENGLISH, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Others;
      }));
      this.othersToChat = this.add(new EnumSetting("ToChat", ChatTranslator.FromLanguage.CHINESE, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Others;
      }));
      this.yours = this.add(new BooleanSetting("Yours", false, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Yours;
      }));
      this.fromMy = this.add(new EnumSetting("FromChat", ChatTranslator.FromLanguage.ENGLISH, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Yours;
      }));
      this.toMy = this.add(new EnumSetting("ToChat", ChatTranslator.FromLanguage.CHINESE, (v) -> {
         return this.page.getValue() == ChatTranslator.Page.Yours;
      }));
      this.googleTranslate = new GoogleTranslate();
   }

   @EventHandler
   public void onSendMessage(SendMessageEvent event) {
      if (this.yours.getValue()) {
         event.message = Objects.requireNonNull(this.translateMy(event.message));
      }
   }

   @EventHandler
   public void onReceiveMessage(ReceiveMessageEvent event) {
      if (mc.world != null && mc.player != null) {
         if (this.others.getValue()) {
            String msg = event.getString();
            Runnable task = () -> {
               this.translateOthers(msg);
            };
            Thread thread = new Thread(task);
            if (thread.getThreadGroup().activeCount() <= 1 && thread.getState() != State.TERMINATED) {
               thread.interrupt();
            } else {
               thread.start();
            }

         }
      }
   }

   private void translateOthers(String text) {
      String incomingMsg = text;
      String translatorPrefix = "\u00a7a[\u00a7b" + ((FromLanguage)this.othersToChat.getValue()).name + "\u00a7a]:\u00a7r ";
      if (!incomingMsg.startsWith("[" + ChatSuffix.INSTANCE.getSuffix() + "]") && !incomingMsg.startsWith(translatorPrefix)) {
         if (!Objects.equals(((FromLanguage)this.othersFromChat.getValue()).value, ((FromLanguage)this.othersToChat.getValue()).value)) {
            String translated = this.googleTranslate.translate(incomingMsg, ((FromLanguage)this.othersFromChat.getValue()).value, ((FromLanguage)this.othersToChat.getValue()).value);
            if (translated != null) {
               Text translationMsg = Text.literal(translatorPrefix).append(Text.literal(translated));
               mc.inGameHud.getChatHud().addMessage(translationMsg);
            }
         }
      }
   }

   private String translateMy(String text) {
      String incomingMsg = text;
      String translatorPrefix = "\u00a7a[\u00a7b" + ((FromLanguage)this.toMy.getValue()).name + "\u00a7a]:\u00a7r ";
      if (!incomingMsg.startsWith("[" + ChatSuffix.INSTANCE.getSuffix() + "]") && !incomingMsg.startsWith(translatorPrefix)) {
         if (((FromLanguage)this.fromMy.getValue()).value == ((FromLanguage)this.toMy.getValue()).value) {
            return text;
         } else {
            String translated = this.googleTranslate.translate(incomingMsg, ((FromLanguage)this.fromMy.getValue()).value, ((FromLanguage)this.toMy.getValue()).value);
            return translated;
         }
      } else {
         return null;
      }
   }

   public enum Page {
      Others,
      Yours;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{Others, Yours};
      }
   }

   public enum FromLanguage {
      AUTO_DETECT("Detect Language", "auto"),
      ARABIC("Arabic", "ar"),
      CHINESE("Chinese", "zh-CN"),
      ENGLISH("English", "en"),
      FRENCH("French", "fr"),
      GERMAN("Deutsch!", "de"),
      ITALIAN("Italian", "it"),
      JAPANESE("Japanese", "ja"),
      KOREAN("Korean", "ko"),
      POLISH("Polish", "pl"),
      PORTUGUESE("Portugese", "pt"),
      RUSSIAN("Russian", "ru"),
      TURKISH("Turkish", "tr");

      private final String name;
      private final String value;

      FromLanguage(String name, String value) {
         this.name = name;
         this.value = value;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static FromLanguage[] $values() {
         return new FromLanguage[]{AUTO_DETECT, ARABIC, CHINESE, ENGLISH, FRENCH, GERMAN, ITALIAN, JAPANESE, KOREAN, POLISH, PORTUGUESE, RUSSIAN, TURKISH};
      }
   }

   public class GoogleTranslate {
      public String translate(String text, String langFrom, String langTo) {
         String html = this.getHTML(text, langFrom, langTo);
         String translated = this.parseHTML(html);
         return text.equalsIgnoreCase(translated) ? null : translated;
      }

      private String getHTML(String text, String langFrom, String langTo) {
         URL url = this.createURL(text, langFrom, langTo);

         try {
            URLConnection connection = this.setupConnection(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String var9;
            try {
               StringBuilder html = new StringBuilder();

               while(true) {
                  String line;
                  if ((line = br.readLine()) == null) {
                     var9 = html.toString();
                     break;
                  }

                  html.append(line + "\n");
               }
            } catch (Throwable var11) {
               try {
                  br.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            br.close();
            return var9;
         } catch (IOException var12) {
            return null;
         }
      }

      private URL createURL(String text, String langFrom, String langTo) {
         try {
            String encodedText = URLEncoder.encode(text.trim(), StandardCharsets.UTF_8);
            String urlString = String.format("https://translate.google.com/m?hl=en&sl=%s&tl=%s&ie=UTF-8&prev=_m&q=%s", langFrom, langTo, encodedText);
            return new URL(urlString);
         } catch (MalformedURLException var6) {
            IOException e = var6;
            throw new RuntimeException(e);
         }
      }

      private URLConnection setupConnection(URL url) throws IOException {
         URLConnection connection = url.openConnection();
         connection.setConnectTimeout(5000);
         connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
         return connection;
      }

      private String parseHTML(String html) {
         String regex = "class=\"result-container\">([^<]*)<\\/div>";
         Pattern pattern = Pattern.compile(regex, 8);
         Matcher matcher = pattern.matcher(html);
         matcher.find();
         String match = matcher.group(1);
         return match != null && !match.isEmpty() ? StringEscapeUtils.unescapeHtml4(match) : null;
      }
   }

   public enum ToLanguage {
      ARABIC("Arabic", "ar"),
      CHINESE("Chinese", "zh-CN"),
      ENGLISH("English", "en"),
      FRENCH("French", "fr"),
      GERMAN("Deutsch!", "de"),
      ITALIAN("Italian", "it"),
      JAPANESE("Japanese", "ja"),
      KOREAN("Korean", "ko"),
      POLISH("Polish", "pl"),
      PORTUGUESE("Portugese", "pt"),
      RUSSIAN("Russian", "ru"),
      TURKISH("Turkish", "tr");

      private final String name;
      private final String value;

      ToLanguage(String name, String value) {
         this.name = name;
         this.value = value;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static ToLanguage[] $values() {
         return new ToLanguage[]{ARABIC, CHINESE, ENGLISH, FRENCH, GERMAN, ITALIAN, JAPANESE, KOREAN, POLISH, PORTUGUESE, RUSSIAN, TURKISH};
      }
   }
}
