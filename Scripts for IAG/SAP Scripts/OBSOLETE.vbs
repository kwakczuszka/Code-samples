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
dim x
x=1
do while x<47  '<------ EDIT THIS NUMBER
session.findById("wnd[0]").maximize
session.findById("wnd[0]/shellcont/shell/shellcont[0]/shell").currentCellColumn = "ICON_EXEC"
session.findById("wnd[0]/shellcont/shell/shellcont[0]/shell").clickCurrentCell
session.findById("wnd[0]").sendVKey 20
session.findById("wnd[1]/usr/cntlG_COMMCONT/shellcont/shell").text = "not relevant" + vbCr + ""
session.findById("wnd[1]/usr/cntlG_COMMCONT/shellcont/shell").setSelectionIndexes 9,9
session.findById("wnd[1]/usr/cmbG_DELREASON").key = "99"
session.findById("wnd[1]/tbar[0]/btn[5]").press
x=x+1
loop