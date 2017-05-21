int pwm1 = 10;
int pwm2 = 11;

int dir1 = 12;
int dir2 = 13;

int incomingByte = 0;

void setup()
{
  pinMode(pwm1, OUTPUT);
  pinMode(pwm1, OUTPUT);
  pinMode(dir1, OUTPUT);
  pinMode(dir2, OUTPUT);

  digitalWrite(pwm1, LOW);
  digitalWrite(pwm2, LOW);
  digitalWrite(dir1, LOW);
  digitalWrite(dir2, LOW);

  Serial.begin(9600);
}

void MotorKontrol(int mdir1, int mdir2, int pwmSpeed)
{
  digitalWrite(dir1, mdir1);
  digitalWrite(dir2, mdir2);
  analogWrite(pwm1, pwmSpeed);
  analogWrite(pwm2, pwmSpeed);
}

void loop()
{
  // This expresses the motion part with serial communication
  if (Serial.available() > 0)
  {
    incomingByte = Serial.read();

    if (incomingByte == 10) // Forward
    {
      MotorKontrol(HIGH, HIGH, 170);
    }
    else if (incomingByte == 20) // Backward
    {
      MotorKontrol(LOW, LOW, 170);
    }
    else if (incomingByte == 30) // Left
    {
      MotorKontrol(HIGH, LOW, 170);
    }
    else if (incomingByte == 40) // Right
    {
      MotorKontrol(LOW, HIGH, 170);
    }
    else // Stop If other data comes
    {
      MotorKontrol(LOW, LOW, 0);
    }
  }
}


