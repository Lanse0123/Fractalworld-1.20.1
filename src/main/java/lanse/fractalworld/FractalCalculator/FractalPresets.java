package lanse.fractalworld.FractalCalculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FractalPresets {

    public static String fractalPreset = "2d_mandelbrot_fractal";
    public static double seedReal = -0.7;
    public static double seedImaginary = 0.27015;
    public static int POWER3D = 8;

    public static final List<String> FRACTALS_2D = Arrays.asList(
            "2d_mandelbrot_fractal", "2d_burning_ship_fractal", "2d_phoenix_fractal", "2d_julia_fractal",
            "2d_random_noise", "2d_collatz_conjecture", "2d_tricorn_fractal", "2d_mandelbrots_weird_cousin_fractal",
            "2d_bridge_fractal", "2d_odd_fractal_that_lanse_cant_think_of_a_name_for_fractal",
            "2d_beam_fractal", "2d_simoncorn_fractal", "2d_stepbrot_fractal", "2d_broken_inverse_mandelbrot_fractal",
            "2d_broken_inverse_burning_ship_fractal", "2d_buffalo_fractal"
    );
    private static final List<String> SEEDED_FRACTALS = Arrays.asList(
            "2d_julia_fractal", "3d_julia_fractal", "3d_mandelbox_fractal", "3d_quintic_mandelbox_fractal"
    );
    public static final List<String> FRACTALS_3D = Arrays.asList(
            "3d_mandelbulb_fractal", "3d_julia_fractal", "3d_burning_ship_fractal", "3d_space_station_fractal",
            "3d_tricorn_fractal", "3d_simoncorn_fractal", "3d_mandelbrots_weird_cousin_fractal",
            "3d_broken_inverse_mandelbrot_fractal", "3d_roche_world_fractal", "3d_sincos_fractal", "3d_meteor_world_fractal",
            "3d_mandelbox_fractal", "3d_mandelfin_fractal", "3d_mandelcross_fractal", "3d_conesmash_fractal",
            "3d_quintic_mandelbox_fractal"
    );

    public static String[] getFractalNames() {
        List<String> fractalCommandList = new ArrayList<>();
        fractalCommandList.addAll(FRACTALS_2D);
        fractalCommandList.addAll(FRACTALS_3D);
        return fractalCommandList.toArray(new String[0]);
    }
    public static boolean isValidPreset(String preset) {
        return FRACTALS_2D.contains(preset.toLowerCase()) || FRACTALS_3D.contains(preset.toLowerCase());
    }
    public static boolean isSeededFractal(String preset) { return SEEDED_FRACTALS.contains(preset.toLowerCase()); }
    public static boolean is3DFractal(String preset) { return FRACTALS_3D.contains(preset.toLowerCase()); }
    public static void setFractalPreset(String preset) { fractalPreset = preset; }
    public static void setSeedValues(double real, double imaginary) {
        seedReal = real;
        seedImaginary = imaginary;
    }
    public static int createFractal(double x, double y) {

        switch (fractalPreset) {
            case "2d_mandelbrot_fractal" -> { return mandelbrot(x, y); }
            case "2d_burning_ship_fractal" -> { return burningShip(x, y); }
            case "2d_buffalo_fractal" -> { return buffaloFractal(x, y); }
            case "2d_julia_fractal" -> { return juliaSet(x, y, seedReal, seedImaginary); }
            case "2d_phoenix_fractal" -> { return phoenixFractal(x, y); }
            case "2d_tricorn_fractal" -> { return tricornFractal(x, y); }
            case "2d_simoncorn_fractal" -> { return simoncornFractal(x, y); }
            case "2d_mandelbrots_weird_cousin_fractal" -> { return mandelbrots_weird_cousin(x, y); }
            case "2d_bridge_fractal" -> { return bridge_fractal(x, y); }
            case "2d_odd_fractal_that_lanse_cant_think_of_a_name_for_fractal" -> { return idkWhatToNameThis(x, y); }
            case "2d_beam_fractal" -> { return beamFractal(x, y); }
            case "2d_stepbrot_fractal" -> { return stepbrotFractal(x, y); }
            case "2d_broken_inverse_mandelbrot_fractal" -> { return brokenInverseMandelbrot(x, y); }
            case "2d_broken_inverse_burning_ship_fractal" -> { return brokenInverseBurningShip(x, y); }

            ///////////////// FRACTALS ABOVE, NON FRACTALS BELOW ///////////////////////

            case "2d_random_noise" -> { return randomNoise(); }
            case "2d_collatz_conjecture" -> { return collatzConjecture(x, y); }
        }
        return -40404;
    }
    public static int[] create3DFractal(double x, double y) {
        switch (fractalPreset) {
            case "3d_mandelbulb_fractal" -> { return mandelbulb3D(x, y); }
            case "3d_mandelbox_fractal" -> { return mandelbox3D(x, y); }
            case "3d_quintic_mandelbox_fractal" -> { return quinticMandelbox3D(x, y); }
            case "3d_burning_ship_fractal" -> { return burningShip3D(x, y); }
            case "3d_julia_fractal" -> { return juliaSet3D(x, y, seedReal, seedImaginary); }
            case "3d_space_station_fractal" -> { return spaceStationFractal3D(x, y); }
            case "3d_tricorn_fractal" -> { return tricornFractal3D(x, y); }
            case "3d_simoncorn_fractal" -> { return simoncornFractal3D(x, y); }
            case "3d_mandelbrots_weird_cousin_fractal" -> { return mandelbrotsWeirdCousin3D(x, y); }
            case "3d_broken_inverse_mandelbrot_fractal" -> { return brokenInverseMandelbrot3D(x, y); }
            case "3d_roche_world_fractal" -> { return rocheWorldFractal3D(x, y); }
            case "3d_sincos_fractal" -> { return sincos3D(x, y); }
            case "3d_meteor_world_fractal" -> { return meteorWorldFractal3D(x, y); }
            case "3d_mandelfin_fractal" -> { return mandelFin3D(x, y); }
            case "3d_mandelcross_fractal" -> { return mandelCross3D(x, y); }
            case "3d_conesmash_fractal" -> { return coneSmash3D(x, y); }
        }
        return new int[]{-40404};
    }


    //////////////// FRACTAL MATH MAGIC FROM HELL BELOW HERE /////////////////////


    public static int mandelbrot(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = 2 * zx * zy + y;
            zx = temp;
            iter++;
        }
        return iter;
    }
    public static int burningShip(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = Math.abs(2 * zx * zy) + y;
            zx = Math.abs(temp);
            iter++;
        }
        return iter;
    }
    public static int buffaloFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy - zx + x;
            zy = Math.abs(2 * zx * zy) - zy + y;
            zx = Math.abs(temp);
            iter++;
        }
        return iter;
    }
    public static int phoenixFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        double prevZx = 0;
        double prevZy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            double newZy = 2 * zx * zy + y;
            newZy += prevZy;
            temp += prevZx;
            prevZx = zx;
            prevZy = zy;
            zx = temp;
            zy = newZy;
            iter++;
        }
        return iter;
    }
    public static int juliaSet(double x, double y, double seedRe, double seedIm) {
        double zx = x;
        double zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double tempZx = zx * zx - zy * zy + seedRe;
            zy = 2 * zx * zy + seedIm;
            zx = tempZx;
            iter++;
        }
        return iter;
    }
    public static int tricornFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iterations = 0;

        while (zx * zx + zy * zy < 4 && iterations < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = -2 * zx * zy + y;
            zx = temp;
            iterations++;
        }
        return iterations;
    }
    public static int simoncornFractal(double x, double y) {
        double zx = x;
        double zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double cx = zx;
            double cy = -zy;
            double rx = Math.abs(zx);
            double ry = zy;
            double re = cx * rx - cy * ry;
            double im = cx * ry + cy * rx;
            double nx = re * re - im * im;
            double ny = 2 * re * im;
            zx = nx + x;
            zy = ny + y;
            iter++;
        }
        return iter;
    }
    public static int mandelbrots_weird_cousin(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx - zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = Math.sin(zx) * Math.tan(zx) - zy * zy + x;
            zy = 1.9 * zx * zy + y;
            zx = temp;
            iter++;
        }
        return iter;
    }
    public static int bridge_fractal(double x, double y) {
        double seed = Math.abs(x + y) % 255 - x / y + 0.787576546648;
        int iter = 0;

        while (iter < FractalGenerator.MAX_ITER && seed < 400) {
            seed = Math.abs(seed * Math.abs(x) - Math.sin(y)) + Math.cos(seed * y) - Math.abs(x - y);
            x = Math.sin(seed * x) - Math.cos(y * seed);
            y = Math.cos(seed * y) + Math.sin(x * seed);
            iter++;
        }
        return iter;
    }
    public static int idkWhatToNameThis(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + Math.atan(x) - Math.sin(y);
            zy = (Math.PI * zx * zy) + Math.asin(y) - 0.4;
            zx = (temp);
            iter++;
        }
        return iter;
    }
    public static int beamFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + y;
            zy = 2.2 * zx * zy + Math.cos(y) - 0.4;
            zx = temp + (temp / (Math.sin(x + 2) * 101.01001));
            iter++;
        }
        return iter;
    }
    public static int stepbrotFractal(double x, double y){
        double zx = 0;
        double zy = 0;
        int iter = 0;
        double MAX_ITER = FractalGenerator.MAX_ITER;

        while (((zx * zx * zx) - (MAX_ITER / 100)) + ((zy * zy * zy) - (MAX_ITER / 100)) < 4 && iter < MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = 2 * zx * zy + y;
            zx = temp;
            iter++;
        }
        return iter;
    }
    public static int brokenInverseMandelbrot(double x, double y) {
        //Broken but beautiful. I am keeping it.
        // It's not a bug, it's a feature!!!1!1!!!!! I'm Learning from Mojang I guess.
        double zx = x;
        double zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx;
            zx = (zx * zx - zy * zy - x) / (zx * zx + zy * zy);
            zy = (-2 * temp * zy - y) / (temp * temp + zy * zy);
            iter++;
        }
        return iter;
    }
    public static int brokenInverseBurningShip(double x, double y) {
        double zx = x;
        double zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx;
            zx = (zx * zx - zy * zy - x) / (zx * zx + zy * zy);
            zy = (-2 * Math.abs(temp) * Math.abs(zy) - y) / (temp * temp + zy * zy);
            zx = Math.abs(zx);
            iter++;
        }
        return iter;
    }

    public static int collatzConjecture(double x, double y) {
        BigInteger currentNumber = BigInteger.valueOf((long) (x * y));
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.valueOf(2);
        BigInteger three = BigInteger.valueOf(3);
        int iterations = 0;

        while (currentNumber.compareTo(one) > 0) {
            if (iterations >= FractalGenerator.MAX_ITER) {
                return FractalGenerator.MAX_ITER + 10;
            }
            if (currentNumber.mod(two).equals(BigInteger.ZERO)) {
                currentNumber = currentNumber.divide(two);
            } else {
                currentNumber = currentNumber.multiply(three).add(one);
            }
            iterations++;
        }
        return iterations;
    }

    private static int randomNoise() {
        Random random = new Random();
        return random.nextInt(FractalGenerator.MAX_ITER) + FractalGenerator.MIN_ITER;
    }

    //////////////// 3D FRACTAL MATH MAGIC FROM THE 9TH RING OF HELL BELOW HERE ///////////////////

    public static int[] mandelbulb3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(Math.sqrt(zx * zx + zz * zz), zy);
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= POWER3D;
                phi *= POWER3D;
                zx = zr * Math.sin(theta) * Math.cos(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.sin(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] mandelbox3D(double x, double z) {
        int[] iterArr = new int[385];
        double scale = 2.0;
        double offset = seedReal;

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                if (zx > 1) zx = 2 - zx;
                else if (zx < -1) zx = -2 - zx;
                if (zy > 1) zy = 2 - zy;
                else if (zy < -1) zy = -2 - zy;
                if (zz > 1) zz = 2 - zz;
                else if (zz < -1) zz = -2 - zz;
                double r2 = zx * zx + zy * zy + zz * zz;
                if (r2 < 1) {
                    zx *= scale / r2;
                    zy *= scale / r2;
                    zz *= scale / r2;
                } else {
                    zx *= scale;
                    zy *= scale;
                    zz *= scale;
                }
                zx += x * offset;
                zy += y * FractalGenerator.scale * offset;
                zz += z * offset;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] quinticMandelbox3D(double x, double z) {
        int[] iterArr = new int[385];
        double scale = 2.0;
        double offset = seedReal;

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                if (zx > 1) zx = 2 - zx - (zx / POWER3D * POWER3D);
                else if (zx < -1) zx = -2 - zx - (zx / POWER3D * POWER3D);
                if (zy > 1) zy = 2 - zy - (zx / POWER3D * POWER3D);
                else if (zy < -1) zy = -2 - zy - (zx / POWER3D * POWER3D);
                if (zz > 1) zz = 2 - zz - (zx / POWER3D * POWER3D);
                else if (zz < -1) zz = -2 - zz - (zx / POWER3D * POWER3D);
                double r2 = Math.pow(zx * zx + zy * zy + zz * zz, 2.5);
                if (r2 < 1) {
                    zx *= scale / r2;
                    zy *= scale / r2;
                    zz *= scale / r2;
                } else {
                    zx *= scale;
                    zy *= scale;
                    zz *= scale;
                }
                zx += x * offset;
                zy += y * FractalGenerator.scale * offset;
                zz += z * offset;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] burningShip3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double tempX = zx * zx - zz * zz - zy * zy + x;
                double tempZ = 2 * Math.abs(zx * zz) + z;
                zy = Math.abs(2 * zx * zy) + y * FractalGenerator.scale;
                zx = Math.abs(tempX);
                zz = Math.abs(tempZ);
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] spaceStationFractal3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            double prevZx = 0;
            double prevZy = 0;
            double prevZz = 0;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double tempX = zx * zx - zy * zy - zz * zz + x;
                double newZy = 2 * zx * zy + y * FractalGenerator.scale;
                double newZz = 2 * zx * zz + z;
                newZy += prevZy;
                tempX += prevZx;
                newZz += prevZz;
                prevZx = zx;
                prevZy = zy;
                prevZz = zz;
                zx = tempX;
                zy = newZy;
                zz = newZz;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] juliaSet3D(double x, double z, double seedRe, double seedIm) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double tempZx = zx * zx - zy * zy - zz * zz + seedRe;
                double newZy = 2 * zx * zy + seedIm;
                double newZz = 2 * zx * zz + seedIm;
                zx = tempZx;
                zy = newZy;
                zz = newZz;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] tricornFractal3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iterations = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iterations < FractalGenerator.MAX_ITER) {
                double tempZx = zx * zx - zy * zy - zz * zz + x;
                double newZy = -2 * zx * zy + y * FractalGenerator.scale;
                double newZz = -2 * zx * zz + z;
                zx = tempZx;
                zy = newZy;
                zz = newZz;
                iterations++;
            }
            iterArr[y + 64] = iterations;
        }
        return iterArr;
    }
    public static int[] simoncornFractal3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double cx = zx;
                double cy = -zy;
                double cz = zz;
                double rx = Math.abs(zx);
                double ry = zy;
                double rz = zz;
                double re = cx * rx - cy * ry - cz * rz;
                double im = cx * ry + cy * rx;
                double newZz = 2 * re * rz;
                double nx = re * re - im * im;
                double ny = 2 * re * im;
                zx = nx + x;
                zy = ny + y * FractalGenerator.scale;
                zz = newZz + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] mandelbrotsWeirdCousin3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = 0;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx - zy * zy - zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double temp = Math.sin(zx) * Math.tan(zx) - zy * zy - zz * zz + x;
                double newZy = 1.9 * zx * zy + y * FractalGenerator.scale;
                double newZz = 1.9 * zx * zz + z;
                zx = temp;
                zy = newZy;
                zz = newZz;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] brokenInverseMandelbrot3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double temp = zx;
                zx = (zx * zx - zy * zy - zz * zz - x) / (zx * zx + zy * zy + zz * zz);
                zy = (-2 * temp * zy - y * FractalGenerator.scale) / (temp * temp + zy * zy + zz * zz);
                zz = (-2 * temp * zz - z) / (temp * temp + zy * zy + zz * zz);
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] rocheWorldFractal3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(Math.sqrt(zx * zx + zz * zz), zy);
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= Math.sin(POWER3D * zr);
                phi *= Math.cos(POWER3D * zr);
                zx = zr * Math.sin(theta) * Math.cos(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.sin(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] sincos3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(Math.sqrt(zx * zx + zz * zz), zy);
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= Math.sin(POWER3D) + Math.cos(zx + zz);
                phi *= Math.cos(POWER3D) + Math.sin(zy);
                zx = zr * Math.sin(theta) * Math.cos(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.sin(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] meteorWorldFractal3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(Math.sqrt(zx * zx + zz * zz), zy);
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= Math.tan(zx + zz);
                phi *= Math.tan(zy + r);
                zx = zr * Math.sin(theta) * Math.cos(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.sin(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] mandelFin3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 25 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(zx * zx - zz * zz, zy + (zz / zy));
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= POWER3D;
                phi *= POWER3D;
                zx = zr * Math.sin(theta) * Math.cos(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.sin(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] mandelCross3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 450 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx + zy * zy + zz * zz);
                double theta = Math.atan2(zx * zx - zz * zz, zy + (zz / zy));
                double phi = Math.atan2(zz, zx);
                double zr = Math.pow(r, POWER3D);
                theta *= POWER3D;
                phi *= POWER3D;
                zx = zr * Math.cos(theta) * Math.sin(phi) + x;
                zy = zr * Math.sin(theta) + y * FractalGenerator.scale;
                zz = zr * Math.cos(theta) * Math.atan(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
    public static int[] coneSmash3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 32767 && iter < FractalGenerator.MAX_ITER) {
                double r = Math.sqrt(zx * zx / 2 + zy * zy * zz - zz * zz * zz);
                double theta = Math.atan2(zx * zx + zz * zz, zy + (zz / zy - 25));
                double phi = Math.atan2(zz, zx * 1.15);
                double zr = Math.pow(r, POWER3D);
                theta *= POWER3D;
                phi *= POWER3D;
                zx = zr * Math.tan(theta) * Math.sin(phi) + x;
                zy = zr * Math.cos(theta) + y * FractalGenerator.scale;
                zz = zr * Math.sin(theta) * Math.atan(phi) + z;
                iter++;
            }
            iterArr[y + 64] = iter;
        }
        return iterArr;
    }
}
