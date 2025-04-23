On Error Resume Next    
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

Dim attachmentName
Dim compareResult
Dim substrings_to_find
Dim attachment_list_row_count
ReDim substrings_to_find(3)

session.findById("wnd[0]").maximize

For j = 0 To session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").RowCount - 1
    Err.Clear
    session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").setCurrentCell j, "DOCID"
    session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").pressToolbarButton "EXT004"
    session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").currentCellColumn = "BITM_DESCR"

    If Err.Number = 0 Then
        attachment_list_row_count = session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").RowCount
        substrings_to_find = Array("ROSSUME", "Support document PDF", "PO Vendor") 
        
        For i = 0 To attachment_list_row_count - 1 
            attachmentName = session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").GetCellValue(i, "BITM_DESCR")
            
            For Each subString In substrings_to_find
                compareResult = InStr(1, attachmentName, subString, vbTextCompare)
                If compareResult > 0 Then
                    session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").currentCellRow = i
                    session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").selectedRows = i
                    session.findById("wnd[1]/usr/cntlCUSTOM_CONTAINER_100/shellcont/shell").doubleClickCurrentCell    
                    session.findById("wnd[1]").sendVKey 12
                    Exit For
                End If
            Next
            
            If compareResult > 0 Then
                Exit For
            End If
        Next
    End If
Next