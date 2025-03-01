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
            "2d_mandelbrot_fractal", "2d_burning_ship_fractal", "2d_man_o_war_fractal", "2d_julia_fractal",
            "2d_random_noise", "2d_collatz_conjecture", "2d_tricorn_fractal", "2d_mandelbrots_weird_cousin_fractal",
            "2d_bridge_fractal", "2d_odd_fractal_that_lanse_cant_think_of_a_name_for_fractal",
            "2d_beam_fractal", "2d_simoncorn_fractal", "2d_stepbrot_fractal", "2d_broken_inverse_mandelbrot_fractal",
            "2d_broken_inverse_burning_ship_fractal", "2d_buffalo_fractal", "2d_cactus_fractal", "2d_hyperbolibrot_fractal",
            "2d_feather_fractal", "2d_sineplane", "2d_perlin_noise", "2d_unstable_noise", "2d_simplex_noise",
            "2d_ridged_noise", "2d_fbm_noise", "2d_circle_island", "2d_turbulent_noise", "2d_mandel_noise",
            "2d_log_noise", "2d_burning_noise", "2d_unnamed1_noise", "2d_custom_fractal", "2d_prime_spiral"
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
    public static boolean isValidPreset(String preset) { return FRACTALS_2D.contains(preset.toLowerCase()) || FRACTALS_3D.contains(preset.toLowerCase()); }
    public static boolean isSeededFractal(String preset) { return SEEDED_FRACTALS.contains(preset.toLowerCase()); }
    public static boolean is3DFractal(String preset) { return FRACTALS_3D.contains(preset.toLowerCase()); }
    public static void setFractalPreset(String preset) { fractalPreset = preset; }
    public static void setSeedValues(double real, double imaginary) {
        seedReal = real;
        seedImaginary = imaginary;
    }
    public static int evaluate(double x, double y){ return CustomFractalCalculator.evaluateFractal(x, y); }
    //This evaluate function is purely cosmetic. It looks better in the switch statement, and it doesn't slow the program down.

    public static int createFractal(double x, double y) {
        //TODO - somehow figure out how to use this with GLSL. calculate all blocks within MAX_RENDER_DISTANCE field
        // of the x and y coords. Then make sure it doesn't repeat this for at least a few seconds. This can
        // probably be stored in a list or array of some sort.
        // Then make sure to split up the worldEdit class function so it uses values from the array instead of
        // constantly calling this function.

        switch (fractalPreset) {

            case "2d_custom_fractal" -> {return evaluate(x, y); }

            case "2d_mandelbrot_fractal" -> {return mandelbrot(x, y); }//"zx*zx - zy*zy + x" "2 * zx * zy + y" "4"
            case "2d_burning_ship_fractal" -> {return burningShip(x, y); }//"abs(zx)*abs(zx) - abs(zy)*abs(zy) + x" "2 * abs(zx) * abs(zy) + y" "4"
            case "2d_buffalo_fractal" -> {return buffaloFractal(x,y); }//"abs(zx*zx - zy*zy - zx + x)" "abs(2 * zx * zy) - zy + y" "4"
            case "2d_julia_fractal" -> {return juliaSet(x, y);}
            case "2d_man_o_war_fractal" -> {return manOfWarFractal(x,y); }//"zx*zx - zy*zy + x + prevZx" "2 * zx * zy + y + prevZy" "4"
            case "2d_tricorn_fractal" -> {return tricornFractal(x, y); }//"zx*zx - zy*zy + x" "-2 * zx * zy + y" "4"
            case "2d_simoncorn_fractal" -> {return simoncornFractal(x, y);}//"re*re - im*im + x" "2 * re * im + y" "4"
            case "2d_cactus_fractal" -> {return cactusFractal(x, y); }//"(zx^3 - 3*zx*zy^2) + ((x - 1) * zx - y * zy) - x" "(3*zx^2*zy - zy^3) + ((x - 1) * zy + y * zx) - y" "4"
            case "2d_mandelbrots_weird_cousin_fractal" -> {return mandelbrots_weird_cousin(x, y); }//"sin(zx) * tan(zx) - zy*zy + x" "1.9 * zx * zy + y" "4"
            case "2d_bridge_fractal" -> {return bridge_fractal(x, y); }
            case "2d_odd_fractal_that_lanse_cant_think_of_a_name_for_fractal" -> {return idkWhatToNameThis(x, y); }
            case "2d_beam_fractal" -> {return beamFractal(x, y); }//"zx*zx - zy*zy + y + (zx*zx - zy*zy + y) / (sin(x + 2) * 101.01001)" "2.2 * zx * zy + cos(y) - 0.4" "4"
            case "2d_stepbrot_fractal" -> {return stepbrotFractal(x, y);}
            case "2d_broken_inverse_mandelbrot_fractal" -> {return brokenInverseMandelbrot(x, y);}
            case "2d_broken_inverse_burning_ship_fractal" -> {return brokenInverseBurningShip(x, y);}
            case "2d_hyperbolibrot_fractal" -> {return hyperbolibrot(x, y);}
            case "2d_feather_fractal" -> {return featherFractal(x, y); }//"((zx^3 - 3*zx*zy^2) / (1 + zx*zx + zy*zy)) + x" "((3*zx^2*zy - zy^3) / (1 + zx*zx + zy*zy)) + y" "4000"

            ///////////////// FRACTALS ABOVE, NON FRACTALS BELOW ///////////////////////

            case "2d_random_noise" -> { return randomNoise(); }
            case "2d_collatz_conjecture" -> { return collatzConjecture(x, y); }
            case "2d_prime_spiral" -> { return primeSpiral((int) x, (int) y); }
            case "2d_sineplane" -> { return sinePlane(x, y); }
            case "2d_perlin_noise" -> { return perlinNoise(x, y); }
            case "2d_unstable_noise" -> { return unstableNoise(x, y); }
            case "2d_simplex_noise" -> { return simplexNoise(x, y); }
            case "2d_ridged_noise" -> { return ridgedNoise(x, y); }
            case "2d_fbm_noise" -> { return fbmNoise(x, y); }
            case "2d_turbulent_noise" -> { return turbulenceNoise(x, y); }
            case "2d_circle_island" -> { return voronoiNoise(x, y); }
            case "2d_mandel_noise" -> { return mandelNoise(x, y); }
            case "2d_log_noise" -> { return logarithmicFractalNoise(x, y); }
            case "2d_burning_noise" -> { return burningNoise(x, y); }
            case "2d_unnamed1_noise" -> { return unamed1(x, y); }
        }
        return -40404;
    }

    public static int[] create3DFractal(double x, double y) {
        switch (fractalPreset) {
            case "3d_mandelbulb_fractal" -> { return mandelbulb3D(x, y); }
            case "3d_mandelbox_fractal" -> { return mandelbox3D(x, y); }
            case "3d_quintic_mandelbox_fractal" -> { return quinticMandelbox3D(x, y); }
            case "3d_burning_ship_fractal" -> { return burningShip3D(x, y); }
            case "3d_julia_fractal" -> { return juliaSet3D(x, y); }
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
    public static int manOfWarFractal(double x, double y) {
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
    public static int juliaSet(double x, double y) {
        double zx = x;
        double zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double tempZx = zx * zx - zy * zy + seedReal;
            zy = 2 * zx * zy + seedImaginary;
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
    public static int cactusFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double zx2 = zx * zx - zy * zy;
            double zy2 = 2 * zx * zy;
            double zx3 = zx * zx2 - zy * zy2;
            double zy3 = zx * zy2 + zy * zx2;
            double cx = x - 1;
            double zcx = cx * zx - y * zy;
            double zcy = cx * zy + y * zx;
            double tempX = zx3 + zcx - x;
            double tempY = zy3 + zcy - y;
            zx = tempX;
            zy = tempY;
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

    public static int hyperbolibrot(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4000 && iter < FractalGenerator.MAX_ITER) {
            double expZx = Math.exp(zx);
            double expNegZx = Math.exp(-zx);
            double sinhRe = 0.5 * (expZx - expNegZx) * Math.cos(zy);
            double sinhIm = 0.5 * (expZx + expNegZx) * Math.sin(zy);
            double sinhRe2 = sinhRe * sinhRe - sinhIm * sinhIm;
            double sinhIm2 = 2 * sinhRe * sinhIm;
            double tempX = sinhRe2 + x;
            double tempY = sinhIm2 + y;
            zx = tempX;
            zy = tempY;
            iter++;
        }
        return iter;
    }
    public static int featherFractal(double x, double y) {
        double zx = 0;
        double zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4000 && iter < FractalGenerator.MAX_ITER) {
            double zx2 = zx * zx - zy * zy;
            double zy2 = 2 * zx * zy;
            double zx3 = zx * zx2 - zy * zy2;
            double zy3 = zx * zy2 + zy * zx2;
            double denomRe = 1 + zx * zx;
            double denomIm = zy * zy;
            double denomMag2 = denomRe * denomRe + denomIm * denomIm;
            double denomReInv = denomRe / denomMag2;
            double denomImInv = -denomIm / denomMag2;
            double newRe = (zx3 * denomReInv - zy3 * denomImInv);
            double newIm = (zx3 * denomImInv + zy3 * denomReInv);
            double tempX = newRe + x;
            double tempY = newIm + y;
            zx = tempX;
            zy = tempY;
            iter++;
        }
        return iter;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    private static int primeSpiral(int x, int y) {
        // Find the layer of the spiral (distance from center)
        int layer = Math.max(Math.abs(x), Math.abs(y));
        int maxVal = (2 * layer + 1) * (2 * layer + 1);

        // Determine the number at (x, y) in the spiral
        int n;
        if (y == -layer) {
            n = maxVal - (layer - x);
        } else if (x == -layer) {
            n = maxVal - (2 * layer) - (layer - y);
        } else if (y == layer) {
            n = maxVal - (4 * layer) - (x + layer);
        } else { // x == layer
            n = maxVal - (6 * layer) - (y + layer);
        }

        // Prime check
        if (n < 2 || (n % 2 == 0 && n != 2)) return 32767;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return 32767; // Not prime
        }

        return 30; // Prime number found
    }

    private static int randomNoise() {
        Random random = new Random();
        return random.nextInt(FractalGenerator.MAX_ITER) + FractalGenerator.MIN_ITER;
    }

    public static int sinePlane(double x, double y) {
        double sinValue = (Math.sin(x) + Math.sin(y)) / 2;
        return (int) (FractalGenerator.MIN_ITER + (sinValue + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
    }

    public static int perlinNoise(double x, double y) {
        double frequency = 0.7;
        double amplitude = (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2.0;
        double noiseValue = Math.sin(frequency * x) * Math.cos(frequency * y);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * amplitude);
    }
    public static int unstableNoise(double x, double y) {
        double frequency = (x + y * y) % 72 / Math.sin(x);
        double amplitude = (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2.0;
        double noiseValue = Math.sin(frequency * x) * Math.cos(frequency * y);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * amplitude);
    }
    public static int simplexNoise(double x, double y) {
        double value = Math.sin(x) * Math.sin(y);
        return FractalGenerator.mapToRange(value);
    }
    public static int ridgedNoise(double x, double y) {
        double value = Math.abs(Math.sin(x) * Math.cos(y));
        return FractalGenerator.mapToRange(value);
    }
    public static int fbmNoise(double x, double y) {
        int octaves = 5;
        double persistence = 0.5;
        double lacunarity = 2.0;
        double total = 0;
        double amplitude = 1.0;
        double frequency = 1.0;
        double maxValue = 0;

        for (int i = 0; i < octaves; i++) {
            total += amplitude * Math.sin(frequency * x) * Math.cos(frequency * y);
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        double normalized = total / maxValue;
        return (int) (FractalGenerator.MIN_ITER + (normalized + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
    }
    public static int voronoiNoise(double x, double y) {
        double nearestDist = Double.MAX_VALUE;

        for (int i = 0; i < 10; i++) {
            double seedX = i * 10 + 0.5;
            double seedY = i * 10 + 0.5;
            double dist = Math.sqrt(Math.pow(x - seedX, 2) + Math.pow(y - seedY, 2));
            nearestDist = Math.min(nearestDist, dist);
        }
        return FractalGenerator.mapToRange(nearestDist);
    }
    public static int turbulenceNoise(double x, double y) {
        double value = Math.abs(Math.sin(x)) + Math.abs(Math.cos(y));
        return FractalGenerator.mapToRange(value);
    }
    public static int mandelNoise(double x, double y) {
        // Mandelbrot parameters
        double zx = 0, zy = 0;
        int iter = 0;
        double scale = 0.1;
        x *= scale;
        y *= scale;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = 2 * zx * zy + y;
            zx = temp;
            iter++;
        }

        double noiseValue = Math.sin(iter * zx) + Math.cos(iter * zy);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
    }

    public static int logarithmicFractalNoise(double x, double y) {
        double zx = x, zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = Math.log(zx * zx - zy * zy + x + 1);
            zy = Math.log(Math.abs(2 * zx * zy + y + 1));
            zx = temp;
            iter++;
        }

        double noiseValue = Math.sin(iter * zx) + Math.cos(iter * zy);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
    }
    public static int burningNoise(double x, double y) {
        double zx = 0, zy = 0;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx - zy * zy + x;
            zy = Math.abs(2 * zx * zy) + y;
            zx = Math.abs(temp);
            iter++;
        }

        double noiseValue = Math.sin(zx) * Math.cos(zy * iter);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
    }

    public static int unamed1(double x, double y) {
        double zx = x, zy = y;
        int iter = 0;

        while (zx * zx + zy * zy < 4 && iter < FractalGenerator.MAX_ITER) {
            double temp = zx * zx * zx - 3 * zx * zy * zy + x;
            zy = 3 * zx * zx * zy - zy * zy * zy + y;
            zx = temp;
            iter++;
        }

        double noiseValue = Math.sin(iter * zx) * Math.cos(iter * zy);
        return (int) (FractalGenerator.MIN_ITER + (noiseValue + 1) * (FractalGenerator.MAX_ITER - FractalGenerator.MIN_ITER) / 2);
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
    public static int[] juliaSet3D(double x, double z) {
        int[] iterArr = new int[385];

        for (int y = 320; y >= -64; y--) {
            double zx = x;
            double zy = y * FractalGenerator.scale;
            double zz = z;
            int iter = 0;

            while (zx * zx + zy * zy + zz * zz < 4 && iter < FractalGenerator.MAX_ITER) {
                double tempZx = zx * zx - zy * zy - zz * zz + seedReal;
                double newZy = 2 * zx * zy + seedImaginary;
                double newZz = 2 * zx * zz + seedImaginary;
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