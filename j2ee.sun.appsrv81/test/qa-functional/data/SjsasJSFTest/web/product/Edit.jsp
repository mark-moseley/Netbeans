<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>Edit Product</title>
    </head>
    <body>
        <f:view>
            <h:messages errorStyle="color: red" infoStyle="color: green" layout="table"/>
            <h1>Edit product</h1>
            <h:form>
                <h:inputHidden value="#{product.product}" immediate="true"/>
                <h:panelGrid columns="2">
                    <h:outputText value="ProductId:"/>
                    <h:outputText value="#{product.product.productId}" title="ProductId" />
                    <h:outputText value="PurchaseCost:"/>
                    <h:inputText id="purchaseCost" value="#{product.product.purchaseCost}" title="PurchaseCost" />
                    <h:outputText value="QuantityOnHand:"/>
                    <h:inputText id="quantityOnHand" value="#{product.product.quantityOnHand}" title="QuantityOnHand" />
                    <h:outputText value="Markup:"/>
                    <h:inputText id="markup" value="#{product.product.markup}" title="Markup" />
                    <h:outputText value="Available:"/>
                    <h:inputText id="available" value="#{product.product.available}" title="Available" />
                    <h:outputText value="Description:"/>
                    <h:inputText id="description" value="#{product.product.description}" title="Description" />
                    <h:outputText value="ManufactureId:"/>
                    <h:selectOneMenu id="manufactureId" value="#{product.product.manufactureId}" title="ManufactureId">
                        <f:selectItems value="#{product.manufactureIds}"/>
                    </h:selectOneMenu>
                    <h:outputText value="ProductCode:"/>
                    <h:selectOneMenu id="productCode" value="#{product.product.productCode}" title="ProductCode">
                        <f:selectItems value="#{product.productCodes}"/>
                    </h:selectOneMenu>
                </h:panelGrid>
                <h:commandLink action="#{product.edit}" value="Save"/>
                <br>
                <h:commandLink action="product_list" value="Show All Product"/>
                <br>
                <a href="/SjsasJSFTest/index.jsp">Back to index</a>
            </h:form>
        </f:view>
    </body>
</html>
