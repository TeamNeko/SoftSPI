#include <errno.h>
#include <stdio.h>
#include <stdint.h>
#include <time.h>
#include <unistd.h>

#include <wiringPi.h>
#include <jni.h>

struct spiport {
  int cs;
  int clk;
  int mosi;
  int miso;
  long speed;
};

static inline void nsleep(long nanos)
{
  int r;
  struct timespec tv;
  tv.tv_sec = 0;
  tv.tv_nsec = nanos;

  do {
    r = nanosleep(&tv, &tv);
  } while((r==-1) && (errno == EINTR));
}

struct spiport getSpiStruct(JNIEnv* env, jclass class, jobject spi)
{
  struct spiport result;

  result.cs = (*env)->GetIntField(env, spi, (*env)->GetFieldID(env, class, "cs", "I"));
  result.clk = (*env)->GetIntField(env, spi, (*env)->GetFieldID(env, class, "clk", "I"));
  result.mosi = (*env)->GetIntField(env, spi, (*env)->GetFieldID(env, class, "mosi", "I"));
  result.miso = (*env)->GetIntField(env, spi, (*env)->GetFieldID(env, class, "miso", "I"));
  result.speed = (*env)->GetIntField(env, spi, (*env)->GetFieldID(env, class, "speed", "J"));

  return result;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
  wiringPiSetup();
  return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL Java_org_teamneko_softspi_SoftSPI_initialize(JNIEnv * env, jclass class, jobject spi)
{
  struct spiport port = getSpiStruct(env, class, spi);

  //Set Chip Select, Clock and Master Out as output
  pinMode (port.cs, OUTPUT);
  pinMode (port.clk, OUTPUT);
  pinMode (port.mosi, OUTPUT);

  //Set all pins to low
  digitalWrite(port.cs, HIGH);
  digitalWrite(port.clk, HIGH);

  //Set Master In as input
  pinMode (port.miso, INPUT);
}

JNIEXPORT jbyteArray JNICALL Java_org_teamneko_softspi_SoftSPI_readWrite(JNIEnv * env, jclass class, jobject spi, jbyteArray data)
{

  int i;
  jsize j;
  jbyte in, out;
  struct spiport port = getSpiStruct(env, class, spi);

  jsize arraySize = (*env)->GetArrayLength(env, data);
  jbyte* outputData = (*env)->GetByteArrayElements(env, data, JNI_FALSE);

  jbyteArray input = (*env)->NewByteArray(env, arraySize);
  jbyte* inputData = (*env)->GetByteArrayElements(env, input, JNI_FALSE);

  //Assert Chip Select
  digitalWrite(port.cs, LOW);

  for(j = 0; j < arraySize; j++)
  {
    in = 0;
    out = outputData[j];

    //Write address cycle
    for(i = 0; i < 8; i++) {
      //Deassert Clock
      digitalWrite(port.clk, LOW);

      //Write current bit
      digitalWrite(port.mosi, (out & 0x80) >> 7);

      //Sleep required time
      nsleep(500000000 / port.speed);

      //Assert Clock for at least 2 usec
      digitalWrite(port.clk, HIGH);

      //Read current bit
      in = (in << 1) | digitalRead(port.miso);

      //Sleep required time
      nsleep(500000000 / port.speed);

      out <<= 1;
    }

    inputData[j] = in;
  }

  //Deassert Chip Select
  digitalWrite(port.cs, HIGH);

  (*env)->ReleaseByteArrayElements(env, data, outputData, 0);
  (*env)->ReleaseByteArrayElements(env, input, inputData, 0);
  return input;
}
