#include <SoftwareSerial.h>

SoftwareSerial sSerial(9, 10); // TX, RX
String BUFFER = "";
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
   // read device output if available.
   if(sSerial.available()) {
     // while there is more to be read, keep reading.
     while(sSerial.available()) {
       BUFFER += (char)sSerial.read();
     }
     Serial.println(BUFFER);
     
     if(BUFFER == "TurnOn") {
       digitalWrite(LED, HIGH);
     }
     if(BUFFER == "TurnOff") {
       digitalWrite(LED, LOW);
     }
     
     BUFFER = "";
   }
  
   // read user input if available.
   if(Serial.available()){
       delay(10);
       sSerial.write(Serial.read());
   }
}
