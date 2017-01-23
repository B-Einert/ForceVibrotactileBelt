//Constants
#define BLUE Serial3
#define PIN_STATUS 13               //status led     
#define fsr1 A0                     //fsr

int LED_status = HIGH;
int pressure;                       //pressure unit
int ffsr1 = 0;
String s1="01";
String n="\n";
String msg;

void setup() {
  Serial.begin(9600);               //serial port initialization
  Serial.println("Bluetooth Serial initialized");
  pinMode(PIN_STATUS, OUTPUT);
  
  BLUE.begin(9600);
}

void loop() {
  // Sendet alles, was eingegeben wird.
  pressure = analogRead(fsr1);
  if (pressure!=ffsr1){
    ffsr1=pressure;
    msg= s1+pressure+n;
    writeString(msg);
    Serial.print(msg);
  }
 /*
  // Gibt alles aus, was empfangen wird:
  if (BLUE.available()){
    while(BLUE.available()>0){
      Serial.write(BLUE.read());
    }
    if(LED_status == HIGH)
      LED_status=LOW;
    else LED_status=HIGH;
    pressure = analogRead(fsr1);
    String s = s1+analogRead(fsr1);
    Serial.println(s);
  
    writeString(s);
  
  }
  */
  digitalWrite(PIN_STATUS,LED_status);
  delay(200);
}

void writeString(String stringData) { // Used to serially push out a String with Serial.write()

  for (int i = 0; i < stringData.length(); i++)
  {
    BLUE.write(stringData[i]);   // Push each char 1 by 1 on each loop pass
  }

}
