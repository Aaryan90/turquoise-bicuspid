#include <SoftwareSerial.h>

SoftwareSerial sSerial(9, 10); // TX, RX
String BUFFER = "";
char DELIMETER = ':';
int LED = 13;

void setup()
{
   pinMode(LED, OUTPUT); 
  
   Serial.begin(9600);
   Serial.println("Type AT commands!");

   // JY-MCU v1.08 defaults to 9600.
   sSerial.begin(9600);
   sSerial.write("AT+BAUD4");
   sSerial.write("AT+NAMETurquoiseBicuspid");
   sSerial.write("AT+PIN1234");
}

void loop()
{
  String type = "";
  int time = 0;
  // read from bluetooth
  if(sSerial.available()) {
    while(sSerial.available()) {
      BUFFER += (char)sSerial.read();
    }
    
    String buff = "";
    for(int i=0; i<BUFFER.length(); i++) {
      buff += BUFFER[i];
      
      // delimeter
      if(BUFFER[i] == ':') {
        buff[i] = '\0';
        type = buff;
        buff = "";
      }
      
      // end of string
      if(i == (BUFFER.length()-1)) {
        time = buff.toInt();
      }
    }
    BUFFER = "";
    
    if(type == "blink") {
      blink(time);
    }
    else if(type == "pulse") {
      
    }
    
  }
  
  // AT commands
  if(Serial.available()){
    delay(10);
    sSerial.write(Serial.read());
  }
}

void blink(int time) {
  for(int i=0; i<5; i++) {
    digitalWrite(LED, HIGH);
    delay(time);
    digitalWrite(LED, LOW);
    delay(time);
  }
}
