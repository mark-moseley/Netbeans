<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>New ProductCode</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>New productCode</h1>
            <h:form>
                <h:panelGrid columns="2">
                    <h:outputText value="ProdCode:"/>
                    <h:inputText id="prodCode" value="#{productCode.productCode.prodCode}" title="ProdCode" />
                    <h:outputText value="DiscountCode:"/>
                    <h:inputText id="discountCode" value="#{productCode.productCode.discountCode}" title="DiscountCode" />
                    <h:outputText value="Description:"/>
                    <h:inputText id="description" value="#{productCode.productCode.description}" title="Description" />
                </h:panelGrid>
                <h:commandLink action="#{productCode.create}" value="Create"/>
                <br>
                <h:commandLink action="productCode_list" value="Show All ProductCode"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>
