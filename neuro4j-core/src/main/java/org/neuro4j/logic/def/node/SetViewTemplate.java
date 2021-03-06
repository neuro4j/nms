package org.neuro4j.logic.def.node;

import org.neuro4j.core.Connected;
import org.neuro4j.logic.LogicContext;
import org.neuro4j.logic.def.LogicBlock;
import org.neuro4j.logic.swf.FlowExecutionException;
import org.neuro4j.logic.swf.FlowInitializationException;
import org.neuro4j.logic.swf.SWFConstants;
import org.neuro4j.logic.swf.SWFParametersConstants;

public class SetViewTemplate extends LogicBlock {
	
	private final static String VIEW_TEMPLATE = SWFConstants.AC_VIEW_TEMPLATE;
	private final static String RENDER_ENGINE_KEY = SWFConstants.RENDER_ENGINE_KEY;
	
	private String staticTemplateName = null;
	
	private String dynamicTemplateName = null;
	
	public SetViewTemplate() {
		super();
	}

	public SetViewTemplate(String name) {
		super();
		lba.setName(name);
	}
	
	public int execute(LogicContext ctx) throws FlowExecutionException 
	{
		String templateName = (String)ctx.get(dynamicTemplateName);

		if (templateName == null)
		{
			templateName = staticTemplateName;
		}


		if (null != templateName)
		{
			ctx.put(VIEW_TEMPLATE, templateName);

		}
		String renderType = getNotEmptyProperty(SWFParametersConstants.VIEW_NODE_RENDER_TYPE);
		if (renderType == null)
		{
			renderType = "jsp";
		}
		ctx.put(RENDER_ENGINE_KEY, renderType);		

		return NEXT;
	}

	
	
	public void load(Connected entity) throws FlowInitializationException
	{
		super.load(entity);		
		staticTemplateName = getNotEmptyProperty(SWFParametersConstants.VIEW_NODE_TEMPLATE_NAME);
		dynamicTemplateName = getNotEmptyProperty(SWFParametersConstants.VIEW_NODE_TEMPLATE_DYNAMIC_NAME);
	}
	
	public void validate(LogicContext fctx) throws FlowExecutionException
	{
		if (staticTemplateName == null && dynamicTemplateName == null)
		{
			throw new FlowExecutionException("SetViewTemplate has wrong configuration.");
		}

	}
}
