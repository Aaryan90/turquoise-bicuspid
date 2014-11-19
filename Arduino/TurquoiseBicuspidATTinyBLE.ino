#include <SoftwareSerial.h>

// ATtiny settings: ATtiny85 @16MHz (internal PLL; 4.3 V BOD), burn bootloader

SoftwareSerial btSerial(3, 2); // TX, RX

// Global
String BUFFER = "";
char DELIMETER = ':';
int BUT = 2;
int BUTT_STATE = 0;
int TIME_START = 0;
int REDPIN = 0;
int GREPIN = 1;
int BLUPIN = 4;

// Defaults
String TYPE = "blink";   // blink, pulse
int LOOP = 3;            // 3, 2, 1
int TIME = 100;          // 500, 250, 100, 50 (milliseconds)
int REPT = 15000;        // 30000, 15000, 5000, 3000, -1 (milliseconds)
int RED = 255;           // 0-255
int GRE = 255;           // 0-255
int BLU = 255;           // 0-255

boolean REPEATING = false;

void setup()
{
   pinMode(BUT, INPUT); 
   pinMode(REDPIN, OUTPUT);
   pinMode(GREPIN, OUTPUT);
   pinMode(BLUPIN, OUTPUT);

   // AC-BT v4 defaults to 9600.
   btSerial.begin(9600);
   //btSerial.write("AT+RENEW"); // Reset all settings.
   delay(250);
   btSerial.write("AT+ROLE0"); // Slave mode (0: Peripheral, 1: Central, default: 0)
   delay(250);
   btSerial.write("AT+MODE2"); // (0: Tranmission, 1: PIO+0, 2: Remote Control+0, default: 0)
   delay(250);
   btSerial.write("AT+IMME1"); // (0: work immediately, 1: wait for AT+ commands, wait for AT+START to resume work, default: 0) Don't enter transmission mode until told. ("AT+IMME0" is wait until "AT+START" to work. "AT+WORK1" is connect right away.)
   delay(250);
   btSerial.write("AT+BAUD0");
   delay(250);
   btSerial.write("AT+NAMETurquoiseB");
   delay(250);
   btSerial.write("AT+PASS123456");
   delay(250);
   btSerial.write("AT+START"); // Work immediately when AT+IMME1 is set.
   
   TIME_START = millis();
}

void loop()
{
  // clear button
  BUTT_STATE = digitalRead(BUT);
  if(BUTT_STATE == HIGH) {
    REPEATING = false;
  }
  // read from bluetooth
  if(btSerial.available()) {
    digitalWrite(BLUPIN, HIGH);
    while(btSerial.available()) {
      BUFFER += (char)btSerial.read();
      delay(1);
    }
    
    String buff = "";
    int index = 0;
    for(int i=0; i<BUFFER.length(); i++) {
      // copy buffer
      buff += BUFFER[i];
      
      // clear buffer
      if(BUFFER[i] == ':') {
        buff = "";
      }
      
      // get a value
      if(BUFFER[i+1] == ':') {
        if(index == 0) {
          TYPE = buff;
          index++;
        }
        else if(index == 1) {
          LOOP = buff.toInt();
          index++;
        }
        else if(index == 2) {
          TIME = buff.toInt();
          index++;
        }
        else if(index == 3) {
          REPT = buff.toInt();
          if(REPT > 0)
          {
            REPEATING = true;
          }
          else if(REPT == -1) {
            REPEATING = false;
          }
          index++;
        }
        else if(index == 4) {
          RED = HEXToRGB(buff[0], buff[1]);
          GRE = HEXToRGB(buff[2], buff[3]);
          BLU = HEXToRGB(buff[4], buff[5]);
          index++;
        }
        buff = "";
      }
    }
    // clear buffer
    BUFFER = "";
    
    if(TYPE == "blink") {
      blinkRGB(LOOP, TIME, RED, GRE, BLU);
    }
    else if(TYPE == "pulse") {
      pulseRGB(LOOP, TIME, RED, GRE, BLU);
    }
    else if(TYPE == "clear") {
      REPEATING = false;
    }
    else {
      blinkRGB(LOOP, TIME, RED, GRE, BLU);
    }
  }
  
  // repeat
  int time_end = millis();
  int time_diff = time_end - TIME_START;
  if(time_diff > REPT) {
    TIME_START = millis();
    if(REPEATING) {
      blinkRGB(LOOP, TIME, RED, GRE, BLU);
    }
  }
}

// blinkRGB - blinks an RGB LED
// params:
//   looper: int - amount of times blink the LED
//   time: int - time for the delay between on/off
//   red: int - red 
//   gre: int - green
//   blu: int - blue
void blinkRGB(int looper, int time, int red, int gre, int blu) {
  for(int i=0; i<looper; i++) {
    analogWrite(REDPIN, red);
    analogWrite(GREPIN, gre);
    analogWrite(BLUPIN, blu);
    delay(time);
    analogWrite(REDPIN, 0);
    analogWrite(GREPIN, 0);
    analogWrite(BLUPIN, 0);
    delay(time);
  }
}

// pulse - pulse an RGB LED
// params:
//   looper: int - amount of times pulse the LED
//   time: int - time for the delay between on/off
//   red: int - red 
//   gre: int - green
//   blu: int - blue
void pulseRGB(int looper, int time, int red, int gre, int blu) {
  int DELAY = 1;
  if(time == 500) {
    DELAY = 8;
  }
  else if(time == 250) {
    DELAY = 5;
  }
  else if(time == 100) {
    DELAY = 3;
  }
  else if(time == 50) {
    DELAY = 1;
  }
  for(int i=0; i<looper; i++) {
    for(int j=0; j<255; j++) {
      if(j <= red) {
        analogWrite(REDPIN, j);
      }
      if(j <= gre) {
        analogWrite(GREPIN, j);
      }
      if(j <= blu) {
        analogWrite(BLUPIN, j);
      }
      delay(DELAY);
    }
    for(int k=255; k>=0; k--) {
      if(k <= red) {
        analogWrite(REDPIN, k);
      }
      if(k <= gre) {
        analogWrite(GREPIN, k);
      }
      if(k <= blu) {
        analogWrite(BLUPIN, k);
      }
      delay(DELAY);
    }
  }
}

int HEXToRGB(char first, char second) {
  int f = CharToHEX(first);
  int s = CharToHEX(second);
  return (f*16)+s;
}

int CharToHEX(char x) {
  int y = 0;
  if(x == '1') y = 1;
  else if(x == '2') y = 2;
  else if(x == '3') y = 3;
  else if(x == '4') y = 4;
  else if(x == '5') y = 5;
  else if(x == '6') y = 6;
  else if(x == '7') y = 7;
  else if(x == '8') y = 8;
  else if(x == '9') y = 9;
  else if(x == 'a') y = 10;
  else if(x == 'b') y = 11;
  else if(x == 'c') y = 12;
  else if(x == 'd') y = 13;
  else if(x == 'e') y = 14;
  else if(x == 'f') y = 15;
  return y;
}
