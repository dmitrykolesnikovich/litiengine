package de.gurkenlabs.util;

import java.util.Random;

public class MathUtilities {
  private static Random RANDOM = new Random();

  public static boolean randomBoolean() {
    return RANDOM.nextDouble() < 0.5;
  }

  public static int randomSign() {
    return randomBoolean() ? 1 : -1;
  }

  public static boolean probabilityIsTrue(double probability) {
    return RANDOM.nextDouble() < probability;
  }

  public static double randomInRange(double min, double max) {
    if (min == max) {
      return min;
    }
    
    if(min > max){
      throw new IllegalArgumentException("min value is > than max value");
    }
    
    return min + RANDOM.nextDouble() * (max - min);
  }
}
