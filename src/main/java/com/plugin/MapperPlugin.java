package com.plugin;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * xxxMapper扩展插件
 * @author lintengyue
 *
 */
public class MapperPlugin extends PluginAdapter {

    private static final String DEFAULT_Mapper_SUPER_CLASS = "com.cwgj.service.erp.mapper.BaseMapper";
    
    //暂存entity， 待优化
    private FullyQualifiedJavaType entityJavaType = null;
    
    //暂存主键类型
    private FullyQualifiedJavaType primaryKey = null;
    
    //暂存实体名称
    private String entityName = null;    
    
    private String mapperTargetDir;
    
    private String mapperTargetPackage;
    
    private String mapperSuperClass;
  
    private ShellCallback shellCallback = null;
 
    public MapperPlugin() {
        shellCallback = new DefaultShellCallback(false);
    }
 
    /**
     * 检验参数 + 日志输出
     * @param warnings
     * @return
     */
    public boolean validate(List<String> warnings) {
        mapperTargetDir = properties.getProperty("targetProject");
        boolean valid = stringHasValue(mapperTargetDir);
 
        mapperTargetPackage = properties.getProperty("targetPackage");
        boolean valid2 = stringHasValue(mapperTargetPackage);
 
        mapperSuperClass = properties.getProperty("mapperSuperClass");
        if (!stringHasValue(mapperSuperClass)) {
            mapperSuperClass = DEFAULT_Mapper_SUPER_CLASS;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n【MapperPlugin init】\r\n");
        sb.append("【读取目标生成目录（targetProject）：" + mapperTargetDir + "】\r\n");
        sb.append("【读取目标包目录（targetPackage）：" + mapperTargetPackage + "】\r\n");
        sb.append("【Mapper基础接口：" + mapperSuperClass + "】\r\n");
        warnings.add(sb.toString());        
        return valid && valid2;
    }

    /**
     * 扩展Mapper方法
     * 实现com.mumogu.diy.mapper.BaseMapper<T, PK> 
     * @author lintengyue
     */
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
    	
    	//构造器
        JavaFormatter javaFormatter = this.context.getJavaFormatter(); 

        //目标集合
        //修改GeneratedJavaFile
        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
               
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {  
        	// * This interface describes methods common to all Java compilation units (Java * classes, interfaces, and enums).
            CompilationUnit unit = javaFile.getCompilationUnit();          
            //The Class FullyQualifiedJavaType. 完整类名
            //getFullyQualifiedName()   com.mumogu.diy.mapper.TAreaMapper
            //getFullyQualifiedNameWithoutTypeParameters()  com.mumogu.diy.mapper.TAreaMapper
            //getPackageName()  com.mumogu.diy.mapper
            //getShortName() TAreaMapper
            //getShortNameWithoutTypeArguments()  TAreaMapper
            FullyQualifiedJavaType baseModelJavaType = unit.getType();

            //获取类名
            String shortName = baseModelJavaType.getShortName();
            
            //获取entity
            if (baseModelJavaType.getPackageName().endsWith("entity")) {
           	
            	//此处需要优化 
            	entityJavaType = baseModelJavaType;
            	entityName = shortName;
            }

            //待优化
            GeneratedJavaFile mapperJavafile = null;
                       
            if (baseModelJavaType.getPackageName().equals(mapperTargetPackage)) { // 扩展Mapper
            	
            	//接口名  xxxMapper
                Interface mapperInterface = new Interface(mapperTargetPackage + "." + shortName);
                //接口访问类型
                mapperInterface.setVisibility(JavaVisibility.PUBLIC);
                
                //接口注释
                mapperInterface.addJavaDocLine("/**");
                mapperInterface.addJavaDocLine(" * MyBatis Generator工具自动生成" + date2Str(new Date()));
                mapperInterface.addJavaDocLine(" * ");
                mapperInterface.addJavaDocLine(" * 开发人员可以在此新增Mapper方法");
                mapperInterface.addJavaDocLine(" * @author lintengyue");
                mapperInterface.addJavaDocLine(" */");
                
                //com.mumogu.diy.mapper.BaseMapper
                FullyQualifiedJavaType mapperSuperType = new FullyQualifiedJavaType(mapperSuperClass);

                // 添加泛型支持  和 导入包
                if(null != entityJavaType) {
                    mapperSuperType.addTypeArgument(entityJavaType);
                    if(null != primaryKey) {
                    	mapperSuperType.addTypeArgument(primaryKey);
                    }else {
//                    	mapperSuperType.addTypeArgument(entityJavaType.getPrimitiveTypeWrapper().getLongInstance());
                    }
                              
                    mapperInterface.addImportedType(entityJavaType);
                }
              
                //添加导入包
                mapperInterface.addImportedType(baseModelJavaType);
                mapperInterface.addImportedType(mapperSuperType);
                
                //添加父类
                mapperInterface.addSuperInterface(mapperSuperType);

                //构建输出目标
                mapperJavafile = new GeneratedJavaFile(mapperInterface, mapperTargetDir, javaFormatter);
                mapperJavaFiles.add(mapperJavafile);           	
            }
        }
        
        //返回目标修改集合
        return mapperJavaFiles;
    }
    
    
    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method,
            Interface interfaze, IntrospectedTable introspectedTable) {
    	primaryKey = method.getParameters().get(0).getType();
        return true;
    }
    
    private String date2Str(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(date);
    }
    
    
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub		
		//创建Select查询
		XmlElement select = new XmlElement("select");
		select.addAttribute(new Attribute("id", "selectByEntity"));
		select.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		select.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		select.addElement(new TextElement("SELECT"));
				
		XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
		includeElement.addAttribute(new Attribute("refid", "Base_Column_List")); //$NON-NLS-1$ //$NON-NLS-2$
		select.addElement(includeElement);
		select.addElement(new TextElement("FROM  " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		
		XmlElement trimElement = new XmlElement("trim"); //$NON-NLS-1$
		trimElement.addAttribute(new Attribute("prefix", "where")); //$NON-NLS-1$ //$NON-NLS-2$
		trimElement.addAttribute(new Attribute("prefixOverrides", "and")); //$NON-NLS-1$ //$NON-NLS-2$

		StringBuilder sb = new StringBuilder();
		for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
			XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
			sb.setLength(0);
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" != null"); //$NON-NLS-1$
			isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
			sb.setLength(0);
			sb.append(" AND ");
			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			sb.append(" = "); //$NON-NLS-1$
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
			isNotNullElement.addElement(new TextElement(sb.toString()));
			trimElement.addElement(isNotNullElement);
		}		
	    select.addElement(trimElement);
				
		XmlElement parentElement = document.getRootElement();
		parentElement.addElement(select);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}
    
}
