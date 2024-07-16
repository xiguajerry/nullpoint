package me.nullpoint.mod.commands;

import java.util.List;
import java.util.Objects;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.Wrapper;

public abstract class Command implements Wrapper {
   protected final String name;
   protected final String description;
   protected final String syntax;

   public Command(String name, String description, String syntax) {
      this.name = Objects.requireNonNull(name);
      this.description = Objects.requireNonNull(description);
      this.syntax = Objects.requireNonNull(syntax);
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getSyntax() {
      return this.syntax;
   }

   public abstract void runCommand(String[] var1);

   public abstract String[] getAutocorrect(int var1, List<String> var2);

   public void sendUsage() {
      String var10000 = this.getName();
      CommandManager.sendChatMessage("\u00a7b[!] \u00a7fUsage: \u00a7e" + var10000 + " " + this.getSyntax());
   }
}
