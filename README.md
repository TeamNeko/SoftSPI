# SoftSPI
### **[Javadoc](https://teamneko.github.io/SoftSPI/)**
## How-to-use

The constructor is the following:
```java
public SoftSPI(int cs, int miso, int mosi, int clk)
```

Once you have instanciated your SoftSPI object, you can use 
```java
public byte read(byte address)
```
to read the byte at the specified address and

```java
public void write(byte address, byte value)
```
to write to the specified value at the specified address.
 
## Use Case
This an SPI port implemented in software, originally for the CatBox project. It allows any GPIO to be used for any function on the SPI Bus.

It is currently hard-coded at 250KHz, but could be chosen programatically at a later date. The 4 SPI Pins (cs, clk, miso, mosi) are chosen when instanciating the class.

Also, it is not an implementation that could be used with just any SPI device. It writes an 6-bit register address with a read-write bit as its MSB and then reads or write a single byte. It was done so to easily interface a MFRC522 RFID Reader.

It is however a proof-of-concept that a software SPI is easy to implement and can work at least up to 250KHz.

No metric were taken on this software SPI, it just works. We cannot say how precise it is, and the precision is sure to drop with higher bus speeds.
