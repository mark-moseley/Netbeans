<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
    may be used to endorse or promote products derived from this software without
    specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
  THE POSSIBILITY OF SUCH DAMAGE.
-->
<jsp:root version="2.1" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:webuijsf="http://www.sun.com/webui/webuijsf">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <webuijsf:page binding="#{Vehicles.page1}" id="page1">
            <webuijsf:html binding="#{Vehicles.html1}" id="html1">
                <webuijsf:head binding="#{Vehicles.head1}" id="head1">
                    <webuijsf:link binding="#{Vehicles.link1}" id="link1" url="/resources/stylesheet.css"/>
                </webuijsf:head>
                <webuijsf:body binding="#{Vehicles.body1}" id="body1" style="-rave-layout: grid">
                    <webuijsf:form binding="#{Vehicles.form1}" id="form1">
                        <div style="position: absolute; left: 0px; top: 0px">
                            <jsp:directive.include file="Header.jspf"/>
                        </div>
                        <h:panelGrid binding="#{Vehicles.content}" id="content" style="left: 0px; top: 240px; position: absolute" styleClass="contents">
                            <h:panelGrid binding="#{Vehicles.contentGrid}" id="contentGrid">
                                <h:panelGrid binding="#{Vehicles.messagePanel}" id="messagePanel" style="width: 100%">
                                    <webuijsf:messageGroup binding="#{Vehicles.messageGroup1}" id="messageGroup1"/>
                                </h:panelGrid>
                                <h:panelGrid binding="#{Vehicles.paddingPanel}" id="paddingPanel" style="height: 20px; width: 100%"/>
                                <h:panelGrid binding="#{Vehicles.dataGrid}" id="dataGrid" style="">
                                    <webuijsf:table augmentTitle="false" binding="#{Vehicles.vehicles}" id="vehicles" paginateButton="true"
                                        paginationControls="true" title="Vehicles" width="720">
                                        <webuijsf:tableRowGroup binding="#{Vehicles.tableRowGroup1}"
                                            emptyDataMsg="You have not added any vehicles. Use the Add button to add a vehicle." id="tableRowGroup1" rows="2"
                                            sourceData="#{Vehicles.ownerDataProvider}" sourceVar="currentRow">
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn1}" headerText="State" id="tableColumn1" sort="STATE.STATENAME">
                                                <webuijsf:staticText binding="#{Vehicles.staticText1}" id="staticText1" text="#{currentRow.value['STATE.STATENAME']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn2}" headerText="License Plate" id="tableColumn2" sort="VEHICLE.LICENSEPLATE">
                                                <webuijsf:staticText binding="#{Vehicles.staticText2}" id="staticText2" text="#{currentRow.value['VEHICLE.LICENSEPLATE']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn3}" headerText="Make" id="tableColumn3" sort="VEHICLE.MAKE">
                                                <webuijsf:staticText binding="#{Vehicles.staticText3}" id="staticText3" text="#{currentRow.value['VEHICLE.MAKE']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn4}" headerText="Model" id="tableColumn4" sort="VEHICLE.MODEL">
                                                <webuijsf:staticText binding="#{Vehicles.staticText4}" id="staticText4" text="#{currentRow.value['VEHICLE.MODEL']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn5}" headerText="Color" id="tableColumn5" sort="VEHICLE.COLOR">
                                                <webuijsf:staticText binding="#{Vehicles.staticText5}" id="staticText5" text="#{currentRow.value['VEHICLE.COLOR']}"/>
                                            </webuijsf:tableColumn>
                                            <webuijsf:tableColumn binding="#{Vehicles.tableColumn6}" id="tableColumn6">
                                                <webuijsf:button actionExpression="#{Vehicles.delete_action}" binding="#{Vehicles.delete}" id="delete" text="Delete"/>
                                            </webuijsf:tableColumn>
                                        </webuijsf:tableRowGroup>
                                    </webuijsf:table>
                                </h:panelGrid>
                                <webuijsf:button actionExpression="#{Vehicles.add_action}" binding="#{Vehicles.add}" id="add" text="Add"/>
                            </h:panelGrid>
                        </h:panelGrid>
                        <div style="position: absolute; left: 0px; top: 600px">
                            <jsp:directive.include file="Footer.jspf"/>
                        </div>
                    </webuijsf:form>
                </webuijsf:body>
            </webuijsf:html>
        </webuijsf:page>
    </f:view>
</jsp:root>
