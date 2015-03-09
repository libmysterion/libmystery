package com.mystery.libmystery.bytes;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;
import javafx.scene.image.Image;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;


public class ByteFunctions {

    public static ByteBuffer toByteBuffer(byte[] bytes) {
        byte[] copy = copy(bytes);
        return ByteBuffer.wrap(copy);
    }

    public static byte[] copy(byte[] bytes) {
        byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }

    public static byte[] join(byte[] first, byte[] second) {
        byte[] combinedData = new byte[second.length + first.length];
        System.arraycopy(first, 0, combinedData, 0, first.length);
        System.arraycopy(second, 0, combinedData, first.length, second.length);
        return combinedData;
    }

    public static byte[] serialize(Serializable obj, boolean len) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(baos)) {
            outputStream.writeObject(obj);
            if (len) {
                byte[] lenBytes = ByteFunctions.integerToBytes(baos.size());
                return join(lenBytes, baos.toByteArray());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] serialize(Serializable obj) {
        return serialize(obj, false);
    }

    public static <T extends Serializable> T deSerialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream objectinputstream = new ObjectInputStream(bais)) {
            return (T) objectinputstream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace(); // wont happen
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int bytesToInteger(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public static byte[] integerToBytes(int a) {
        return new byte[]{
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),
            (byte) ((a >> 8) & 0xFF),
            (byte) (a & 0xFF)
        };
    }

    public static long bytesToLong(byte[] b) {
        return (((long) b[0] << 56)
                + ((long) (b[1] & 255) << 48)
                + ((long) (b[2] & 255) << 40)
                + ((long) (b[3] & 255) << 32)
                + ((long) (b[4] & 255) << 24)
                + ((b[5] & 255) << 16)
                + ((b[6] & 255) << 8)
                + ((b[7] & 255)));
    }

    public static byte[] longToBytes(long v) {
        return new byte[]{
            (byte) (v >>> 56),
            (byte) (v >>> 48),
            (byte) (v >>> 40),
            (byte) (v >>> 32),
            (byte) (v >>> 24),
            (byte) (v >>> 16),
            (byte) (v >>> 8),
            (byte) (v)};

    }

    public static byte[] shortToBytes(short v) {
        return new byte[]{
            (byte) (v >>> 8),
            (byte) (v)};
    }

    public static short bytesToShort(byte[] b) {
        return (short) (((b[0] & 255) << 8) + ((b[1] & 255)));
    }

    public static byte[] doubleToBytes(double d) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(d);
        return bytes;
    }

    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static byte[] imageToJpegBytes(BufferedImage img, float quality) {
        Iterator<ImageWriter> i = ImageIO.getImageWritersByFormatName("jpeg");
        // Just get the first JPEG writer available  
        ImageWriter jpegWriter = i.next();

        ImageWriteParam param = jpegWriter.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            ImageOutputStream o = new ImageOutputStreamImpl() {
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public int read() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            jpegWriter.setOutput(o);
            // ImageIO.write(img, "jpg", out);
            jpegWriter.write(null, new IIOImage(img, null, null), param);
            jpegWriter.dispose();
            return out.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

    public static BufferedImage bytesToBufferedImage(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            return null;
        }
    }

    public static Image bytesToImage(byte[] jpegBytes) {
        return new Image(new ByteArrayInputStream(jpegBytes));
    }
}
