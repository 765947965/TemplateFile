package com.alfredxl.templatefile.dialog;

import com.alfredxl.templatefile.bean.Template;
import com.alfredxl.templatefile.constant.Constants;
import com.alfredxl.templatefile.factory.DynamicDataFactory;
import com.alfredxl.templatefile.factory.FormatFactory;
import com.alfredxl.templatefile.factory.SimpleDocumentListener;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class SettingJPanel extends JPanel implements ActionListener {

    private List<Template> dynamicList;
    private TemplateTableModel dynamicModel;
    private JBTable dynamicTable;

    private List<Template> templateList;
    private TemplateTableModel classModel;
    private JBTable classTable;

    private JTextArea jTextAreaCode;
    private JTextPane jTextAreaCodeFormat;

    private boolean isModified;
    private boolean showFormatCode;
    private FormatFactory formatFactory;
    private ListSelectionListener listSelectionListener;

    public SettingJPanel() {
        this(false, null);
    }

    public SettingJPanel(boolean showFormatCode, FormatFactory formatFactory) {
        this.showFormatCode = showFormatCode;
        this.formatFactory = formatFactory;
        dynamicList = DynamicDataFactory.getDynamicData();
        dynamicModel = new TemplateTableModel(dynamicList, dynamicList, DynamicDataFactory.getTitle(showFormatCode), showFormatCode,
                formatFactory);
        dynamicTable = new JBTable(dynamicModel);

        templateList = DynamicDataFactory.getTemplateData();
        classModel = new TemplateTableModel(templateList, dynamicList, DynamicDataFactory.getClassTitle(showFormatCode),
                showFormatCode, formatFactory);
        classTable = new JBTable(classModel);
        buildRuleFilePanel();
    }

    private void buildRuleFilePanel() {
        setLayout(new VerticalLayout(10));
        setBorder(JBUI.Borders.empty(10));
        add(new JLabel(Constants.SETTING_PANEL_TIPS));

        // 动态参数配置
        add(setJBTable(dynamicTable, new TemplateAction.AddOrEditLocationAction(dynamicList,
                        dynamicModel, dynamicTable, this, true, showFormatCode, false),
                new TemplateAction.RemoveLocationAction(dynamicList, dynamicModel, dynamicTable, this),
                new TemplateAction.AddOrEditLocationAction(dynamicList,
                        dynamicModel, dynamicTable, this, true, showFormatCode, true),
                Constants.DYNAMIC_TITLE));

        // 类模板配置
        JPanel classContainer = setJBTable(classTable, new TemplateAction.AddOrEditLocationAction(templateList,
                        classModel, classTable, this, false, false, false),
                new TemplateAction.RemoveLocationAction(templateList, classModel, classTable, this),
                new TemplateAction.AddOrEditLocationAction(templateList,
                        classModel, classTable, this, false, false, true),
                Constants.TEMPLATE_TITLE);
        JLabel infoLabel = new JLabel(Constants.CODE_TIPS, SwingConstants.LEFT);
        infoLabel.setBorder(JBUI.Borders.empty(8, 0, 4, 0));
        classContainer.add(infoLabel, BorderLayout.SOUTH);
        add(classContainer);

        // 模板代码区域
        JPanel jPanelCode = new JPanel(new HorizontalLayout(5));
        jTextAreaCode = new JTextArea();
        JBScrollPane jScrollPane = new JBScrollPane(jTextAreaCode);
        jScrollPane.setPreferredSize(new Dimension(750, 500));
        jPanelCode.add(jScrollPane);
        if (showFormatCode) {
            jTextAreaCodeFormat = new JTextPane();
            jTextAreaCodeFormat.setEditable(false);
            JBScrollPane jScrollPaneFormat = new JBScrollPane(jTextAreaCodeFormat);
            jScrollPaneFormat.setPreferredSize(new Dimension(750, 500));
            jPanelCode.add(jScrollPaneFormat);
        }
        add(jPanelCode);

        //创建监听
        createListener();
    }

    private void createListener() {
        addTableListener(dynamicTable, dynamicModel, dynamicList);
        addTableListener(classTable, classModel, templateList);
        listSelectionListener = e -> {
            final int selectedIndex = classTable.getSelectedRow();
            if (selectedIndex == -1) {
                jTextAreaCode.setText("");
                if (jTextAreaCodeFormat != null) {
                    jTextAreaCodeFormat.setText("");
                }
                return;
            }
            String oldData = templateList.get(selectedIndex).getData();
            jTextAreaCode.setText(oldData);
            reSetTextAreaCodeFormat(oldData);
        };
        classTable.getSelectionModel().addListSelectionListener(listSelectionListener);
        jTextAreaCode.getDocument().addDocumentListener(new SimpleDocumentListener() {

            protected void change() {
                final int selectedIndex = classTable.getSelectedRow();
                if (selectedIndex == -1) {
                    return;
                }
                String data = jTextAreaCode.getText();
                templateList.get(selectedIndex).setData(jTextAreaCode.getText());
                reSetTextAreaCodeFormat(data);
            }
        });
    }

    private void reSetTextAreaCodeFormat(String oldData) {
        if (jTextAreaCodeFormat != null) {
            String data;
            if (showFormatCode && formatFactory != null) {
                data = formatFactory.formatData(dynamicList, oldData);
            } else {
                data = oldData;
            }
            jTextAreaCodeFormat.setText(data);
        }
    }


    private void addTableListener(JBTable jbTable, AbstractTableModel model, List<Template> listData) {
        jbTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int columnIndex = jbTable.columnAtPoint(e.getPoint()); //获取点击的列
                    int rowIndex = jbTable.rowAtPoint(e.getPoint()); //获取点击的行
                    if (columnIndex == 0) {
                        Template template = listData.get(rowIndex);
                        template.setEnabled(!template.isEnabled());
                        model.fireTableDataChanged();
                        isModified = true;
                    }
                }
            }
        });
    }


    private JPanel setJBTable(JBTable jbTable, AnActionButtonRunnable addAction, AnActionButtonRunnable removeAction,
                              AnActionButtonRunnable editAction, String title) {
        jbTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jbTable.setStriped(true);
        jbTable.getTableHeader().setReorderingAllowed(false);
        setTableColumnWidth(jbTable);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(jbTable);
        toolbarDecorator.setAddAction(addAction);
        toolbarDecorator.setRemoveAction(removeAction);
        toolbarDecorator.setEditAction(editAction);
        toolbarDecorator.setPreferredSize(new Dimension(500, 120));
        JPanel container = new JPanel(new BorderLayout());
        container.add(new TitledSeparator(title), BorderLayout.NORTH);
        container.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        return container;
    }

    private void setTableColumnWidth(JBTable table) {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(60);
        column.setWidth(80);
        column.setPreferredWidth(80);
        column.setMaxWidth(80);
    }

    public boolean isModified() {
        return isModified;
    }

    @Override
    public void change() {
        setModified(true);
        classModel.fireTableDataChanged();
        listSelectionListener.valueChanged(null);
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public List<Template> getDynamicList() {
        return dynamicList;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }
}
