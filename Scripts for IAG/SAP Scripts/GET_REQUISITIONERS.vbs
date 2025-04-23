If Not IsObject(application) Then
   Set SapGuiAuto  = GetObject("SAPGUI")
   Set application = SapGuiAuto.GetScriptingEngine
End If
If Not IsObject(connection) Then
   Set connection = application.Children(0)
End If
If Not IsObject(session) Then
   Set session    = connection.Children(0)
End If
If IsObject(WScript) Then
   WScript.ConnectObject session,     "on"
   WScript.ConnectObject application, "on"
End If

dim excelFilePath 
excelFilePath = "C:\Users\u245849\OneDrive - International Airlines Group\Documents\purchaseorders.xlsx"  '<------ PUT EXCEL FILE PATH HERE
dim numRows
numRows = 70  '<------ EDIT THIS VARIABLE

set objExcel = CreateObject("Excel.Application")
set objWorkbook = objExcel.Workbooks.Open(excelFilePath)
set objSheet = objWorkbook.Sheets("Sheet1") 


For i = 1 To numRows
   session.findById("wnd[0]").maximize
   session.findById("wnd[0]/tbar[1]/btn[17]").press
   session.findById("wnd[1]/usr/subSUB0:SAPLMEGUI:0003/ctxtMEPO_SELECT-EBELN").text = objSheet.Cells(i, 1).Text
   session.findById("wnd[1]").sendVKey 0
   objSheet.Cells(i,2).Value = session.ActiveWindow.Text
Next

objWorkbook.Save
objWorkbook.Close 
objExcel.Quit

Set objSheet = Nothing
Set objWorkbook = Nothing
Set objExcel = Nothing 
