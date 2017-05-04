import java.io.*;
import java.lang.Object;
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.util.*;

public class LSB_Hide_Message
{
	private String imagePath;
	private String imagename;
	private String imageType; // tested with bmp and png file
	private List<String> imageBytes;
	private List<String> dataBytes;
	private final int startIndex = 60;
	private final char stx = (char) 2;
	private final char etx = (char) 3;

	public LSB_Hide_Message(String imagePath) {
		this.imagePath = imagePath;
	}

	private void extractBytes() throws IOException {
		imageBytes = new ArrayList<String>();

		//Open the image
		File imgPath = new File(imagePath);
		BufferedImage imgBuffer = ImageIO.read(imgPath);

		imagename = imgPath.getName().substring(0, imgPath.getName().indexOf('.'));
		imageType = imgPath.getName().substring(imgPath.getName().indexOf('.') + 1, imgPath.getName().length());

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ImageIO.write(imgBuffer, "bmp", b);
		b.flush();
		byte[] imageData = b.toByteArray();
		b.close();


		for (int i = 0; i < imageData.length; i ++ ) {

			String s = String.format("%8s", Integer.toBinaryString(imageData[i] & 0xFF)).replace(' ', '0');

			imageBytes.add(s);
		}
	}

	private void getDataToHide() {
		dataBytes = new ArrayList<String>();

		String data = "" + stx;

		//get text data from user input
		System.out.print("Enter Secret Message : ");
		Scanner sc = new Scanner (System.in);
		data += sc.nextLine();
		data += etx;

		char [] charData = data.toCharArray();

		//convert data to binary
		for (int i = 0; i < charData.length; i++) {

			String s = String.format("%8s", Integer.toBinaryString(charData[i] & 0xFF)).replace(' ', '0');

			dataBytes.add(s);
		}
	}

	private boolean compressImage() {
		byte[] imageDataInDecimal = new byte[imageBytes.size()];
		for (int i = 0; i < imageBytes.size(); i++) {
			int imageData = Integer.parseInt(imageBytes.get(i), 2);

			imageDataInDecimal[i] = (byte)imageData;
		}

		try {
			// convert byte array back to BufferedImage
			BufferedImage bImageFromConvert = ImageIO.read(new ByteArrayInputStream(imageDataInDecimal));

			ImageIO.write(bImageFromConvert, imageType, new File(imagename+"-stego."+imageType));
		}
		catch (IOException ex) {
			System.out.println(ex.getMessage());
			return false;
		}

		return true;
	}

	private boolean canUsed(String binary) {
		return Integer.parseInt(binary, 2) == 255 || Integer.parseInt(binary, 2) == 254 ? false : true;
	}

	public boolean hide_message() {

		try {
			//get image data and convert to binary
			extractBytes();
		}
		catch(IOException ex) {
			return false;
		}

		//get data from user input and convert to binary
		getDataToHide();

		int counter = startIndex;
		for (int i = 0; i < dataBytes.size(); i ++) {

			char [] databit = dataBytes.get(i).toCharArray();
			for (int j = 0; j < databit.length; j ++) {
				String imageByte = imageBytes.get(counter).substring(0, imageBytes.get(i).length() - 1) + databit[j];

				if (canUsed(imageByte)) {
					imageBytes.set(counter, imageByte);
 					counter++;
				}
				else {
					j--;
					counter++;
				}
			}
		}

		if (compressImage()) return true;
		return false;

	}

	public String getMessage(int startindex, int endindex) {
		int counter = 0;
		String resultInBinary = "";
		String result = "";
		boolean checkStx = true;
		for (int i = startindex; i < endindex; i++) {
			if (canUsed(imageBytes.get(i))) {
				if (counter == imageBytes.get(i).length()) {
					int resultInInt = Integer.parseInt(resultInBinary, 2);
					char charRes = (char) resultInInt;
					if (checkStx) {
						if (charRes != stx) return "";
						else checkStx = false;
					}
					else {
						if (charRes != etx) {
							result += charRes;
						}
						else {
							break;
						}
					}
					resultInBinary = "";
					counter = 0;
					i--;
				}
				else {
					resultInBinary += imageBytes.get(i).substring(imageBytes.get(i).length() - 1, imageBytes.get(i).length());
					counter ++;
				}
			}
			else {
				endindex ++;
			}
		}

		return result;
	}

	public String extract_message() {
		try{
			extractBytes();	
		}
		catch (IOException ex) {

		}
		
		int counter = 0;
		String resultInBinary = "";
		String result = "";
		boolean checkStx = true;
		for (int i = startIndex; i < imageBytes.size(); i++) {
			if (canUsed(imageBytes.get(i))) {
				if (counter == imageBytes.get(i).length()) {
					int resultInInt = Integer.parseInt(resultInBinary, 2);
					char charRes = (char) resultInInt;
					if (checkStx) {
						if (charRes != stx) return "";
						else checkStx = false;
					}
					else {
						if (charRes != etx) {
							result += charRes;
						}
						else {
							break;
						}
					}
					resultInBinary = "";
					counter = 0;
					i--;
				}
				else {
					resultInBinary += imageBytes.get(i).substring(imageBytes.get(i).length() - 1, imageBytes.get(i).length());
					counter ++;
				}
			}
		}

		return result;		
	}

	public static void main(String [] args)
	{
		Scanner sc = new Scanner(System.in);

		while(true) {
			System.out.println("Select Option");
			System.out.println("1. Create Hidden Message");
			System.out.println("2. Open Hidden Message");
			System.out.println("3. Exit");
			System.out.print("Your Choice : ");
			int select = sc.nextInt();

			switch (select) {
				case 1 :
				{
					System.out.print("Open image : ");
					String filename = sc.next();
					LSB_Hide_Message ez1 = new LSB_Hide_Message(filename);
					System.out.println("\n=======================================================");
					if (ez1.hide_message()) System.out.println("Secret image created successfully");
					else System.out.println("Failed to create secret image");
				} break;

				case 2 :
				{
					System.out.print("Open secret image : ");
					String filename = sc.next();
					LSB_Hide_Message ez2 = new LSB_Hide_Message(filename);
					String message = ez2.extract_message();

					if (message != "") {
						System.out.println("\n=======================================================");
						System.out.println("Secret Message : " + message);
					}
					else {
						System.out.println("\n=======================================================");
						System.out.println("This image is not contain any message");
					} 
				} break;

				case 3:
				{
					System.exit(0);
				}

				default: 
				{
					System.out.println("\n=======================================================");
					System.out.println("Wrong input!");
				}
			}

			System.out.println("=======================================================\n");
		}
		
		
	}
}