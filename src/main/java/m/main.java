package m;

import java.util.Random;

public class main {
    public static void main(String[] args) {
        // 定义数组
        String[] array = { "1", "2", "2", "3", "3", "3" };

        // 创建一个随机数生成器
        Random random = new Random();

        // 从数组中随机取出一个元素
        int index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);

        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);

        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);

        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);
        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);
        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);
        index = random.nextInt(array.length);
        System.out.println("随机取出的元素是：" + array[index]);
    }
}