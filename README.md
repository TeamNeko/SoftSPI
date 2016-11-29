# SoftSPI
### **[Javadoc](https://teamneko.github.io/SoftSPI/)**
## How-to-use

The constructor is the following. It creates an immutable that describes an SPI Port.
```java
public SoftSPI(int cs, int miso, int mosi, int clk, long speed)
```
Instanciating the object initializes the SPI port. Now you can just use:
```java
public static byte[] readWrite(SoftSPI spi, byte[] data)
```
to read from and write to the SPI port. It writes the values into data on the port and returns the value read in a byte array.

## Use Case
This an SPI port implemented in software for the Raspberry Pi, originally for the CatBox project. It allows any GPIO to be used for any function on the SPI Bus. It is a proof-of-concept that a software SPI is easy to implement and has been tested at 250kb/s.

No metric were taken on this software SPI, it just works. We cannot say how precise it is, and the precision is sure to drop with higher bus speeds.
