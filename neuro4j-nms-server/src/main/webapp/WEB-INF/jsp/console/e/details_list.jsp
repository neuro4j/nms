<%@ page import="org.neuro4j.core.*" %>
<%@ page import="org.neuro4j.core.rel.*" %>
<%@ page import="java.util.*"%>

<%
Connected entity = (Connected) request.getAttribute("entity");
%>

<table style="margin-bottom: 3px;" width="100%"  border="0" cellspacing="0" cellpadding="0" class="l r t b">
    <tr>
        <td width="150px" align="left" valign="top" class="b">
          Name
        </td>
        <td align="left" valign="top" class="l b">
          ${entity.name}
        </td>
    </tr>
    <tr>
        <td width="150px" align="left" valign="top" class="b">
          ID
        </td>
        <td align="left" valign="top" class="l b">
          ${entity.uuid}
        </td>
    </tr>
    <tr>
        <td width="150px" align="left" valign="top" class="r b">
        Properties
        </td>
        <td align="left" valign="top" class="b">
           <table>
             <%
                for (String key : entity.getPropertyKeys()) {
             %> 
                <tr>
                  <td valign="top"><%=key%> : </td>
                  <td valign="top"><%=entity.getProperty(key)%>&nbsp;</td>
                </tr>           
            <%
              }
            %> 


           </table>
        
        </td>
    </tr>
    <tr>
        <td width="150px" align="left" valign="top" class="b">
        Relations
        </td>
        <td align="left" valign="top" class="l b">
       
        <table width="100%"  border="0" cellspacing="0" cellpadding="0" class="">
          <tr>
           
          </tr>
			<%
			Map<String, List<Connected>> groupedRelationMap = entity.groupConnectedByName();
			for (String groupName : groupedRelationMap.keySet())
			{
			    %>
		          <tr>
		            <td class="hp"><%= groupName %> (<%= groupedRelationMap.get(groupName).size() %>) </td> 
		          </tr>
                <%
                  for (Connected rel : groupedRelationMap.get(groupName))
                  {
                %> 
                  <tr>
                    <td class=""><a href="relation-details?storage=${storage}&uuid=<%=rel.getUuid()%>"><%=rel.getName()%></a> 
                        [<%
                         	for (String key : rel.getPropertyKeys())
                                                   {
                         %> 
                          <%=key%>: <%=rel.getProperty(key)%>
                          &nbsp; 
                        <%
                          }
                        %>] 
                    </td> 
                  </tr>
			    <%
                  } // for (Relation rel : groupedRelationMap.get(groupName))
			}
			%>        
        </table>
        
        </td>
    </tr>

    <tr>
        <td width="150px" align="left" valign="top" class="r">
        Representations
        </td>
        <td align="left" valign="top" >
           <table width="100%">
             <%
                for (Representation rep : entity.getRepresentations()) {
             %> 
                <tr>
                  <td valign="top" class="b">
                    <a href="representation-details?storage=${storage}&id=<%=rep.getUuid()%>">download</a>  
                    <br/>
		             <%
		                for (String repKey : rep.getPropertyKeys()) {
		             %> 
                          <%=repKey%>: <%=rep.getProperty(repKey)%>
	                    <br/>                  
		            <%
		              }
		            %> 
                  </td>
                  
                </tr>           
            <%
              }
            %> 


           </table>
        
        </td>
    </tr>

</table>

