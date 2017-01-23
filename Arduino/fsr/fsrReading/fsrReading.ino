int fsr1 = A0;                      //fsr pin
int pressure;                       //pressure unit
int LEDpin = 13;                    //led pin

void setup() {
  pinMode(LEDpin,OUTPUT);
  Serial.begin(9600);               //serial port initialization
}

void loop() {
  int pressure = analogRead(fsr1);  //reading of the fsr value
  Serial.println(pressure);         //writing of the value to the serial port
  if(pressure>10){                  //led reaction
    digitalWrite(LEDpin, HIGH);
  }
  else{
    digitalWrite(LEDpin, LOW);
  }
  delay(200);
}
