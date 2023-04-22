package com.game.util;

import java.util.*;

/**
 * 生成随机数Service
 *
 * @author derek
 * @version 1.0
 */
public class RandomUtil {

    public static Random random = new Random();

    /**
     * 是否在范围内 如(5/100)则有5%几率返回值为true
     *
     * @param minPercent 分子
     * @param maxPercent 分母
     * @return 是否在范围内
     */
    public static boolean isInTheLimits(Integer minPercent, Integer maxPercent) {
        if (random.nextInt(maxPercent) + 1 <= minPercent) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 随机下标([0~size-1])，不重复
     *
     * @param size 最大数量
     * @param num  需要数量
     */
    public static Set<Integer> randomIndexNotDuplicate(int size, int num) {
        if (num > size)
            num = size;
        int remain = size - num;
        Set<Integer> result = new HashSet<>(num);
        if (remain < num) {
            Set<Integer> exInclude = new HashSet<>(remain);
            while (exInclude.size() < remain) {
                exInclude.add(RandomUtil.getRandomNumber(size));
            }
            for (int i = 0; i < size; i++) {
                if (!exInclude.contains(i)) {
                    result.add(i);
                }
            }
        } else {
            while (result.size() < num) {
                result.add(RandomUtil.getRandomNumber(size));
            }
        }
        return result;
    }

    public static int[] randomIndexFromList(List<Integer> indexList, int length) {
        int[] randomArray = new int[length];
        if (length == 0) {
            return randomArray;
        }
        Set<Integer> set = new TreeSet<Integer>();
        while (true) {
            int index = random.nextInt(indexList.size());
            set.add(indexList.get(index));
            indexList.remove(index);
            if (set.size() >= length) {
                break;
            }
        }

        Iterator<Integer> iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            randomArray[i] = iterator.next();
            i++;
        }

        return randomArray;
    }

    /**
     * 生成随机数字数组(大于等于minNum且小于等于maxNum)
     *
     * @param minNum
     * @param maxNum
     * @param length
     * @return int数组(按照从小到大自动排序)
     */
    public static int[] generateRandomNumberArray(int minNum, int maxNum, int length) {
        int[] randomArray = new int[length];
        Set<Integer> set = new TreeSet<Integer>();
        int offset = maxNum - minNum;
        while (true) {
            set.add(random.nextInt(offset) + minNum + 1);
            if (set.size() >= length) {
                break;
            }
        }

        Iterator<Integer> iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            randomArray[i] = iterator.next();
            i++;
        }
        return randomArray;
    }

    /**
     * 生成随机数字数组(大于等于minNum且小于等于maxNum)
     *
     * @param minNum
     * @param maxNum
     * @param length
     * @return int数组(按照从小到大自动排序)
     */
    public static Collection<Integer> generateNumberArray(int minNum, int maxNum, int length) {
        int min = minNum < maxNum ? minNum : maxNum;
        int offset = Math.abs(maxNum - minNum);
        if (length >= offset + 1) {
            Collection<Integer> randomArray = new ArrayList<>(offset + 1);
            for (int i = 0; i < offset + 1; i++) {
                randomArray.add(min + i);
            }
            return randomArray;
        }
        int i = 0;
        Set<Integer> set = new TreeSet<Integer>();
        while (true) {
            int r = random.nextInt(offset);
            while (!set.add(r + min)) {
                r = (r + 1) % offset;
            }
            if (set.size() >= length) {
                break;
            }
        }
        return set;
    }

    /**
     * 在已存在数据中随机选取数字
     *
     * @param existingArray 已存在数组
     * @param num           数量
     * @return
     */
    public static int[] generateRandomNumberArrayFromExistingArray(Integer[] existingArray, int num) {
        int[] randomArray = new int[num];
        Set<Integer> set = new TreeSet<Integer>();
        while (true) {
            set.add(existingArray[random.nextInt(existingArray.length)]);
            if (set.size() >= num) {
                break;
            }
        }
        Iterator<Integer> iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            randomArray[i] = iterator.next();
            i++;
        }
        return randomArray;
    }

    public static int getRandomNumber(int len) {
        return random.nextInt(len);
    }

    public static boolean isInArray(int[] array, int e) {
        for (int i : array) {
            if (i == e) {
                return true;
            }
        }
        return false;
    }

    public static <T> T getOneRandomElement(List<T> coll) {
        int length = coll.size();
        if (length == 0) {
            return null;
        }
        int sel = getRandomNumber(length);
        return coll.get(sel);
    }

    /**
     * 随机出value~value*N之间的整数，precision指定精度 例如：将value=1000，在100%~200%内随机，精度2%
     * 则这样调用randomMultipleN(1000,2,2)
     *
     * @param value       需要随机出倍数的值
     * @param maxMultiple 倍数 >1
     * @param precision   指定精度 >=1
     * @return
     */
    public static int randomMultipleN(int value, int maxMultiple, int precision) {
        // 精度值
        int precisionValue = (int) Math.pow(10, precision);
        // 因子范围
        int factorRange = precisionValue * (maxMultiple - 1);
        int factor = RandomUtil.getRandomNumber(factorRange + 1);

        double temp = value + value * factor / precisionValue;
        int result = (int) Math.round(temp); // 四舍五入
        return result;
    }

    /**
     * 产生min与max之间的一个随机整数
     *
     * @param min
     * @param max
     * @return
     * @Added by 500 [min,max)
     */
    public static int randomBetween(int min, int max) {
        if (min >= max)
            return min;
        return random.nextInt(max - min) + min;
    }

    /**
     * 打乱顺序
     *
     * @param array
     */
    public static void disorder(int[] array) {
        int tmp;
        int tmpIndex;
        if (null != array && 0 < array.length) {
            for (int i = 0; i < array.length; i++) {
                tmpIndex = getRandomNumber(array.length);
                if (i != tmpIndex) {
                    tmp = array[i];
                    array[i] = array[tmpIndex];
                    array[tmpIndex] = tmp;
                }
            }
        }
    }

    public static void main(String[] args) {
        List<Integer> indexList = new ArrayList<Integer>();
        for (int k = 0; k < 8; k++)
            indexList.add(k);
        int[] a = randomIndexFromList(indexList, 2);
        System.out.println(a[0] + "," + a[1]);
        a = randomIndexFromList(indexList, 2);
        System.out.println(a[0] + "," + a[1]);
        a = randomIndexFromList(indexList, 2);
        System.out.println(a[0] + "," + a[1]);
        System.out.println(indexList.size());

        for (Integer r : generateNumberArray(50, 70, 10)) {
            System.out.println(r);
        }
    }
}
