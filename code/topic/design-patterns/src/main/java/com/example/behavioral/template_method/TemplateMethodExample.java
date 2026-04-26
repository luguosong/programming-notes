package com.example.behavioral.template_method;

import java.util.List;

/**
 * 模板方法模式 - 正例
 * DataExporter 定义导出流程骨架，子类只实现差异化的步骤
 */
public class TemplateMethodExample {
    public static void main(String[] args) {
        List<String[]> data = List.of(
            new String[]{"张三", "25", "北京"},
            new String[]{"李四", "30", "上海"}
        );

        new ExcelExporter().export(data); // ✅ 复用相同流程
        System.out.println("---");
        new CsvExporter().export(data);   // ✅ 复用相同流程
    }
}

// 抽象模板：定义导出流程骨架（final 防止子类破坏流程）
abstract class DataExporter {
    // 模板方法（final：流程固定，子类不可修改顺序）
    public final void export(List<String[]> data) {
        openConnection();     // 公共步骤
        validateData(data);   // 公共步骤
        writeHeader();        // 抽象步骤：子类实现
        writeData(data);      // 抽象步骤：子类实现
        closeConnection();    // 公共步骤
        notifyComplete();     // 钩子方法：子类可选重写
    }

    // 公共步骤：所有子类共享
    private void openConnection()  { System.out.println("[" + name() + "] 1. 建立数据库连接"); }
    private void validateData(List<String[]> data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("数据不能为空");
        System.out.println("[" + name() + "] 2. 数据校验通过，共 " + data.size() + " 行");
    }
    private void closeConnection() { System.out.println("[" + name() + "] 5. 关闭连接");     }

    // 钩子方法：有默认实现，子类可按需重写
    protected void notifyComplete() { System.out.println("[" + name() + "] 6. 导出完成");    }

    // 抽象步骤：子类必须实现
    protected abstract String name();
    protected abstract void   writeHeader();
    protected abstract void   writeData(List<String[]> data);
}

// ✅ 具体子类：Excel 导出，只需实现差异化步骤
class ExcelExporter extends DataExporter {
    @Override protected String name() { return "Excel"; }

    @Override
    protected void writeHeader() {
        System.out.println("[Excel] 3. 写入 Excel 表头：姓名\t年龄\t城市");
    }

    @Override
    protected void writeData(List<String[]> data) {
        for (String[] row : data) {
            System.out.println("[Excel] 4. 行：" + String.join("\t", row));
        }
    }
}

// ✅ 具体子类：CSV 导出，只需实现差异化步骤
class CsvExporter extends DataExporter {
    @Override protected String name() { return "CSV"; }

    @Override
    protected void writeHeader() {
        System.out.println("[CSV] 3. 写入 CSV 表头：姓名,年龄,城市");
    }

    @Override
    protected void writeData(List<String[]> data) {
        for (String[] row : data) {
            System.out.println("[CSV] 4. 行：" + String.join(",", row));
        }
    }

    // 重写钩子：CSV 导出后额外压缩
    @Override
    protected void notifyComplete() {
        System.out.println("[CSV] 6. 压缩 CSV 文件...");
        super.notifyComplete();
    }
}
