//#define RAZOR Serial2


String sensorData;
char c;
void setup() {
  Serial.begin(57600);
  Serial2.begin(57600);
  Serial.println("9DOF Test"); 
}
  
void loop() {
  while (Serial2.available()) {  
    if (Serial2.available() >0) {
      c = Serial2.read(); 
      //sensorData += c; //adds c to the sensorData string
    }
  }
  Serial.print(c); //see whatever came in on Serial.read()
  if (sensorData.length() >0) {
    Serial.println(sensorData);  //output the string
    sensorData=""; //resets reString for the next iteration of the loop
  } 
  delay(500);
} 
