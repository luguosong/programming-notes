package com.example.behavioral.template_method;

import java.util.List;

/**
 * 模板方法模式 - 反例
 * 问题：ExcelExporterBad 和 CsvExporterBad 有大量重复逻辑，维护困难
 */
public class TemplateMethodBadExample {
    public static void main(String[] args) {
        List<String[]> data = List.of(
            new String[]{"张三", "25", "北京"},
            new String[]{"李四", "30", "上海"}
        );

        new ExcelExporterBad().export(data);
        new CsvExporterBad().export(data);
    }
}

// ❌ Excel 导出器：包含大量重复流程
class ExcelExporterBad {
    public void export(List<String[]> data) {
        System.out.println("[Excel] 1. 建立数据库连接");       // 重复 ❌
        System.out.println("[Excel] 2. 获取并校验数据");        // 重复 ❌
        // Excel 特有步骤
        System.out.println("[Excel] 3. 写入 Excel 表头：姓名,年龄,城市");
        for (String[] row : data) {
            System.out.println("[Excel] 行：" + String.join("\t", row));
        }
        System.out.println("[Excel] 4. 关闭连接");              // 重复 ❌
        System.out.println("[Excel] 5. 通知完成");               // 重复 ❌
    }
}

// ❌ CSV 导出器：与 Excel 有大量重复代码
class CsvExporterBad {
    public void export(List<String[]> data) {
        System.out.println("[CSV] 1. 建立数据库连接");           // 重复 ❌
        System.out.println("[CSV] 2. 获取并校验数据");           // 重复 ❌
        // CSV 特有步骤
        System.out.println("[CSV] 3. 写入 CSV 表头：姓名,年龄,城市");
        for (String[] row : data) {
            System.out.println("[CSV] 行：" + String.join(",", row));
        }
        System.out.println("[CSV] 4. 关闭连接");                 // 重复 ❌
        System.out.println("[CSV] 5. 通知完成");                  // 重复 ❌
    }
}
