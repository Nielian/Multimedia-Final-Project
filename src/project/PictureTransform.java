package project;

import ij.ImagePlus;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import Jama.Matrix;

/*
 * Trida urcena pro pokrocilou praci s obrazem
 * ma na starosti matice R,G,B a Y,Cb,Cr
 * dela veskere konverze formatu
 */

public class PictureTransform {

	private BufferedImage bImage;
	private ColorModel colorModel;
	private int imageHeight;
	private int imageWidth;

	// barevne komponenty
	private int[][] red;
	private int[][] green;
	private int[][] blue;

	private Matrix y;
	private Matrix cB;
	private Matrix cR;

	// diferencni
	private Matrix dY;
	private Matrix dcB;
	private Matrix dcR;

	private Matrix brightnessQuantMatrix;
	private Matrix colorQuantMatrix;

	// konstuktor
	public PictureTransform(BufferedImage bImage) {
		this.bImage = bImage;
		this.colorModel = bImage.getColorModel();

		this.imageHeight = bImage.getHeight();
		this.imageWidth = bImage.getWidth();

		red = new int[this.imageHeight][this.imageWidth];
		green = new int[this.imageHeight][this.imageWidth];
		blue = new int[this.imageHeight][this.imageWidth];

		y = new Matrix(this.imageHeight, this.imageWidth);
		cB = new Matrix(this.imageHeight, this.imageWidth);
		cR = new Matrix(this.imageHeight, this.imageWidth);

		dY = new Matrix(this.imageHeight, this.imageWidth);
		dcB = new Matrix(this.imageHeight, this.imageWidth);
		dcR = new Matrix(this.imageHeight, this.imageWidth);
		// vytvoreni matric pro kvantizaci
		double[][] array = { { 16, 11, 10, 16, 24, 40, 51, 61 },
				{ 12, 12, 14, 19, 26, 58, 60, 55 },
				{ 14, 13, 16, 24, 40, 57, 69, 56 },
				{ 14, 17, 22, 29, 51, 87, 80, 62 },
				{ 18, 22, 37, 56, 68, 109, 103, 77 },
				{ 24, 35, 55, 64, 81, 104, 113, 92 },
				{ 49, 64, 78, 87, 103, 121, 120, 101 },
				{ 72, 92, 95, 98, 112, 100, 103, 99 } };
		brightnessQuantMatrix = new Matrix(array);
		double[][] array1 = { { 17, 18, 24, 47, 99, 99, 99, 99 },
				{ 18, 21, 26, 66, 99, 99, 99, 99 },
				{ 24, 26, 56, 99, 99, 99, 99, 99 },
				{ 47, 66, 99, 99, 99, 99, 99, 99 },
				{ 99, 99, 99, 99, 99, 99, 99, 99 },
				{ 99, 99, 99, 99, 99, 99, 99, 99 },
				{ 99, 99, 99, 99, 99, 99, 99, 99 },
				{ 99, 99, 99, 99, 99, 99, 99, 99 } };
		colorQuantMatrix = new Matrix(array1);
	}

	// vytvoreni zobrazitelnych RGB matic
	public void getRGB() {
		for (int i = 0; i < this.imageHeight; i++) {
			for (int j = 0; j < this.imageWidth; j++) {
				red[i][j] = colorModel.getRed(this.bImage.getRGB(j, i));
				green[i][j] = colorModel.getGreen(this.bImage.getRGB(j, i));
				blue[i][j] = colorModel.getBlue(this.bImage.getRGB(j, i));
			}
		}
	}

	// vytvoreni zobrazitelnych YCbCr matic
	public void convertRgbToYcbcr() {
		for (int i = 0; i < this.imageHeight; i++) {
			for (int j = 0; j < this.imageWidth; j++) {
				y.set(i, j, 0.257 * red[i][j] + 0.504 * green[i][j] + 0.098
						* blue[i][j] + 16);
				cB.set(i, j, -0.148 * red[i][j] - 0.291 * green[i][j] + 0.439
						* blue[i][j] + 128);
				cR.set(i, j, 0.439 * red[i][j] - 0.368 * green[i][j] - 0.071
						* blue[i][j] + 128);
			}
		}
	}

	// pro vytvoøení modelu RGB
	public ImagePlus setImageFromRGB(int width, int height, int[][] r,
			int[][] g, int[][] b) {
		BufferedImage bImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb[i][j] = new Color(r[i][j], g[i][j], b[i][j]).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}
		return (new ImagePlus("", bImage));
	}

	// Pro vytvoøení modelu jedné komponenty R G B z pole int
	public ImagePlus setImageFromRGB(int width, int height, int[][] x,
			String component) {
		BufferedImage bImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				rgb[i][j] = new Color(x[i][j], x[i][j], x[i][j]).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}
		return (new ImagePlus(component, bImage));
	}

	// Pro vytvoøení modelu jedné komponenty Y Cb Cr z pole Matrix
	public ImagePlus setImageFromRGB(int width, int height, Matrix x,
			String component) {
		BufferedImage bImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		int[][] rgb = new int[height][width];

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				rgb[i][j] = new Color((int) x.get(i, j), (int) x.get(i, j),
						(int) x.get(i, j)).getRGB();
				bImage.setRGB(j, i, rgb[i][j]);
			}
		}
		return (new ImagePlus(component, bImage));
	}

	public void convertYcbcrToRgb() {
		for (int i = 0; i < this.imageHeight; i++) {
			for (int j = 0; j < this.imageWidth; j++) {
				red[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16) + 1.596
						* (cR.get(i, j) - 128));
				if (red[i][j] > 255)
					red[i][j] = 255;
				if (red[i][j] < 0)
					red[i][j] = 0;
				green[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16)
						- 0.813 * (cR.get(i, j) - 128) - 0.391
						* (cB.get(i, j) - 128));
				if (green[i][j] > 255)
					green[i][j] = 255;
				if (green[i][j] < 0)
					green[i][j] = 0;
				blue[i][j] = (int) Math.round(1.164 * (y.get(i, j) - 16)
						+ 2.018 * (cB.get(i, j) - 128));
				if (blue[i][j] > 255)
					blue[i][j] = 255;
				if (blue[i][j] < 0)
					blue[i][j] = 0;
			}
		}
	}

	public Matrix downsample(Matrix mat) {
		Matrix newMat = new Matrix(mat.getRowDimension(),
				mat.getColumnDimension() / 2);
		for (int i = 0; i < mat.getColumnDimension(); i = i + 2) {
			newMat.setMatrix(0, mat.getRowDimension() - 1, i / 2, i / 2,
					mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
		}
		return newMat;
	}

	public Matrix oversample(Matrix mat) {
		Matrix newMat = new Matrix(mat.getRowDimension(), mat.getColumnDimension() * 2);
		for (int i = 0; i < mat.getColumnDimension(); i++) {
			newMat.setMatrix(0, mat.getRowDimension() - 1, 2 * i, 2 * i, mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
			newMat.setMatrix(0, mat.getRowDimension() - 1, 2 * i + 1, 2 * i + 1, mat.getMatrix(0, mat.getRowDimension() - 1, i, i));
		}
		return newMat;
	}

	// transformace a kvantizace
	// vytvoreni transformacni matice pro DCT
	private Matrix getDctMatrix(int size) {
		Matrix dctMatrix = new Matrix(size, size);
		double pom;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i == 0) {
					dctMatrix.set(i, j, 1 / Math.sqrt(size));
				} else {
					pom = Math.sqrt(2 / (double) size)
							* Math.cos((Math.PI * (2 * j + 1) * i)
									/ (2 * (double) size));
					dctMatrix.set(i, j, pom);
				}
			}
		}
		return dctMatrix;
	}

	public Matrix transform(int size, Matrix transformMatrix, Matrix inputMatrix) {
		Matrix out = transformMatrix.times(inputMatrix);
		out = out.times(transformMatrix.transpose());
		return (out);
	}

	public Matrix inverseTransform(int size, Matrix transformMatrix,
			Matrix inputMatrix) {
		Matrix out = transformMatrix.transpose().times(inputMatrix);
		out = out.times(transformMatrix);
		return (out);
	}

	// blokova transformace
	// pokud nejsou bloky 8x8, nekvantuje se
	public Matrix blockTransformation(Matrix input, int blockSize, int q,
			boolean bright) {// pokud je wht false, pouzije se dct
		// q a bright slouzi vyhradne pro poreby kvantizace
		Matrix output = new Matrix(input.getRowDimension(),
				input.getColumnDimension());
		Matrix block = new Matrix(blockSize, blockSize);
		// tady chceme pocet bloku, ne velikost
		for (int i = 0; i < input.getRowDimension() / blockSize; i++) {
			for (int j = 0; j < input.getColumnDimension() / blockSize; j++) {
				// vyjmuti bloku z matice input
				block = input.getMatrix(blockSize * i, (blockSize - 1)
						* (i + 1) + i, blockSize * j, (blockSize - 1) * (j + 1)
						+ j);
				// transformace bloku
				block = transform(blockSize, this.getDctMatrix(blockSize),
						block);
				// kvantizace bloku
				if (blockSize == 8) {
					block = quantization(block, q, bright);
				}

				// vlozeni bloku do matice output
				output.setMatrix(blockSize * i, (blockSize - 1) * (i + 1) + i,
						blockSize * j, (blockSize - 1) * (j + 1) + j, block);
			}
		}
		return output;
	}

	public Matrix inverseBlockTransformation(Matrix input, int blockSize,
			int q, boolean bright) {
		Matrix output = new Matrix(input.getRowDimension(),
				input.getColumnDimension()); // 512x512 px
		Matrix block = new Matrix(blockSize, blockSize);

		for (int i = 0; i < input.getRowDimension() / blockSize; i++) {
			for (int j = 0; j < input.getColumnDimension() / blockSize; j++) {
				// vyjmuti bloku z matice
				block = input.getMatrix(blockSize * i, (blockSize - 1)
						* (i + 1) + i, blockSize * j, (blockSize - 1) * (j + 1)
						+ j);
				// inverzni kvantizace
				if (blockSize == 8) {
					block = inverseQuantization(block, q, bright);
				}

				// inverzni transformace
				block = inverseTransform(blockSize,
						this.getDctMatrix(blockSize), block);

				// vlozeni bloku do matice
				output.setMatrix(blockSize * i, (blockSize - 1) * (i + 1) + i,
						blockSize * j, (blockSize - 1) * (j + 1) + j, block);
			}
		}
		return output;
	}

	public Matrix wideMat(Matrix mat) {
		Matrix newMat = new Matrix(mat.getRowDimension()+16, mat.getColumnDimension()); // zvysi pocet radku o 16, sloupce stejne
		for (int i = 0; i < 8; i++) {
			// 8x zkopiruje prvni a posledni radek	
			newMat.setMatrix(i, i, 0, mat.getColumnDimension()-1, mat.getMatrix(0, 0, 0, mat.getColumnDimension()-1));
			newMat.setMatrix( i+248, i+248, 0, mat.getColumnDimension()-1, mat.getMatrix(mat.getRowDimension()-1, mat.getRowDimension()-1, 0, mat.getColumnDimension()-1));
			}
		newMat.setMatrix(8, 247, 0, mat.getColumnDimension()-1, mat);
		
		return newMat;
	}

	// kvantizace
	public Matrix quantization(Matrix inputBlock, double q, boolean bright) {
		Matrix outputBlock = new Matrix(8, 8);
		Matrix quantMatrix = new Matrix(8, 8);
		double alpha = 1;

		if ((q >= 1) && (q <= 50))
			alpha = 50 / q;
		else if ((q > 50) && (q <= 99))
			alpha = 2 - 2 * q / 100d;

		if (bright == true)
			quantMatrix = brightnessQuantMatrix.times(alpha);
		else
			quantMatrix = colorQuantMatrix.times(alpha);

		outputBlock = inputBlock.arrayRightDivide(quantMatrix);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				outputBlock.set(i, j, (int) outputBlock.get(i, j));
			}
		}

		return outputBlock;
	}

	public Matrix inverseQuantization(Matrix inputBlock, double q,
			boolean bright) {
		Matrix outputBlock = new Matrix(8, 8);
		Matrix quantMatrix = new Matrix(8, 8);
		double alpha = 1;

		if ((q >= 1) && (q <= 50))
			alpha = 50 / q;
		else if ((q > 50) && (q <= 99))
			alpha = 2 - 2 * q / 100d;

		if (bright == true)
			quantMatrix = brightnessQuantMatrix.times(alpha);
		else
			quantMatrix = colorQuantMatrix.times(alpha);

		outputBlock = inputBlock.arrayTimes(quantMatrix);

		return outputBlock;
	}

	// getters and setters
	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Matrix getY() {
		return y;
	}

	public void setY(Matrix y) {
		this.y = y;
	}

	public Matrix getcB() {
		return cB;
	}

	public void setcB(Matrix cB) {
		this.cB = cB;
	}

	public Matrix getcR() {
		return cR;
	}

	public void setcR(Matrix cR) {
		this.cR = cR;
	}

	public Matrix getdY() {
		return dY;
	}

	public void setdY(Matrix dY) {
		this.dY = dY;
	}

	public Matrix getDcB() {
		return dcB;
	}

	public void setDcB(Matrix dcB) {
		this.dcB = dcB;
	}

	public Matrix getDcR() {
		return dcR;
	}

	public void setDcR(Matrix dcR) {
		this.dcR = dcR;
	}

	public int[][] getRed() {
		return red;
	}

	public void setRed(int[][] red) {
		this.red = red;
	}

	public int[][] getGreen() {
		return green;
	}

	public void setGreen(int[][] green) {
		this.green = green;
	}

	public int[][] getBlue() {
		return blue;
	}

	public void setBlue(int[][] blue) {
		this.blue = blue;
	}
}
