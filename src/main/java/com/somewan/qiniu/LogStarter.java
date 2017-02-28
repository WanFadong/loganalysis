package com.somewan.qiniu;

/**
 * Created by wan on 2017/2/27.
 */
public class LogStarter {
    private static LogService logService = new LogService();

    public static void main(String[] args) {
        String[] machines = {"nb252", "xs300"};
        String[] dcs = {"dc3", "dc9"};
        int count = 0;
        for (int i = 0; i < machines.length; i++) {
            for (int j = 0; j < dcs.length; j++) {
                if (i == 0 && j == 1)
                    count += logService.record(machines[i], dcs[j]);
            }
        }
        System.out.println("一共写入日志：" + count);
    }
}
