# FeatherEconomy

FeatherEconomy is a spigot plugin for adding an item based economy to minecraft servers.  
Currently hardcoded to use lapis lazuli as a currency.  
Players can deposit and withdraw lapis between their inventory and their account.  
Players can transfer lapis to other players from account to account.

Support for MySQL/MariaDB or built in SQLite.

### Dependencies:
This plugin depends on [Vault](https://github.com/milkbowl/Vault) 


### Permission Nodes:
    Player permission nodes:

    feather.economy.deposit          -  /deposit [amount]            -  Move lapis: inv -> account.
    feather.economy.balance          -  /balance                     -  Check account balance.
    feather.economy.withdraw         -  /withdraw [amount]           -  Move lapis: account -> inv.
    feather.economy.transfer         -  /transfer [amount] [player]  -  Send lapis to another player.
    
    Administrator permission nodes:

    feather.economy.managebalance    -  view/add/remove/set another player's balance
