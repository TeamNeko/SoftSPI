CC=gcc
CFLAGS=-Wall -fPIC -shared
TARGET=libSoftSpi.so
SOURCE=softspi.c
JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt
LDFLAGS=-lwiringPi

$(TARGET): $(SOURCE)
	$(CC) -o $(TARGET) $(SOURCE) $(CFLAGS) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux $(LDFLAGS)
