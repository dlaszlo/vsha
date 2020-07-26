# Otthon automatizálás

@@@ TODO 

## Üzenetküldés bekonfigurálása

A program Telegram segítségével tud üzenetet küldeni. 

Példák, hogy mikor lehet érdemes üzenetet küldeni: 
- A vízértékelő szenzor jelez (mert csőtörés van)
- Csengett valaki 
- stb..

Beállítások elvégzése az üzenetküldéshez:

1. A telegram-on kell létrehozni egy bot-ot, ezt a BotFather bot-tól kell kérni.
2. Bot létrehozáskor a BotFather a bot nevét, és a userId-jét fogja megkérdezni.
3. A HTTP API-hoz kapott tokent az application.properties-ben kell beállítani a telegram.bot_token beállításban.
4. Létre kell hozni egy private channelt, és meg kell hívni bele a bot-ot admin-ként, és a felhasználókat.
5. A channel-t meg kell nyitni egy webböngészőben, és az channel_id-t az URL-ből kell kinyerni:
    1. Pl. ez az URL: https://web.telegram.org/#/im?p=c1234567890_1234567890123456789
    2. Ekkor a channel ID a c és a _ jel közötti rész, de elé kell írni, hogy -100, tehát ez lesz: -1001234567890 
    3. Tehát a channel ID egy - jel, és 13 db szám.
    4. A kapott channel ID-t az application.properties-ben kell beállítani, a telegram.chat_id beállításban.

