
Option Explicit
Public SapGuiAuto
Public objGui As GuiApplication
Public objConn As GuiConnection
Public session As GuiSession
Dim ws As Worksheet
Dim wsSAP As Worksheet
Dim tbl As ListObject
Dim lastRow As Long
Dim i As Long
Dim dictVAT As Object
Dim paymentDateCol As Integer
Dim endDateCol As Integer
Dim paymentStatusCol As Integer

Sub InvoiceSummary()
    
    Set ws = ThisWorkbook.Worksheets("SmartDOC")
    Set SapGuiAuto = GetObject("SAPGUI")
    Set objGui = SapGuiAuto.GetScriptingEngine
    Set objConn = objGui.Children(0)
    Set session = objConn.Children(0)
    Range("A2").Select
    Range(Selection, Selection.End(xlDown)).Select
    Selection.Copy
    session.FindById("wnd[0]").Maximize
    session.StartTransaction ("/OPT/VIM_VA2")
    'session.FindById("wnd[0]/tbar[1]/btn[17]").Press                                                                'load variant (saved filter set)
    'session.FindById("wnd[1]/usr/txtV-LOW").Text = "FOREIGN_REPORT"                                                 'variant name
    'session.FindById("wnd[1]/usr/txtENAME-LOW").Text = ""                                                           'set "Created by" to be empty
    'session.FindById("wnd[1]").SendVKey 0                                                                           'Enter
    session.FindById("wnd[0]/usr/btn%_S_DOCID_%_APP_%-VALU_PUSH").Press                                             'doc id multiple selection
    'session.FindById("wnd[1]/usr/tabsTAB_STRIP/tabpNOSV").Select                                                    'exclude values
    session.FindById("wnd[1]").SendVKey 24                                                                          'Shift+F12  (references to exclude have to be in the clipboard)
    session.FindById("wnd[1]").SendVKey 8                                                                           'F8 (Execute)

    

    session.FindById("wnd[0]/tbar[1]/btn[8]").Press                                                                 'execute search
    
    session.FindById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").PressToolbarButton "&MB_VARIANT"       'choose layout
    session.FindById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cmbG51_SCREEN-USPEC_LBOX").SetFocus
    session.FindById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").ContextMenu      'set filter
    session.FindById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").SelectContextMenuItem "&FILTER"
    session.FindById("wnd[2]/usr/ssub%_SUBSCREEN_FREESEL:SAPLSSEL:1105/ctxt%%DYN001-LOW").Text = "/EI_INV_SUM"        'choose layout Invoice Summary
    session.FindById("wnd[2]/tbar[0]/btn[0]").Press
    session.FindById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").SelectedRows = "0"
    session.FindById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").ClickCurrentCell
    
    session.FindById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").PressToolbarContextButton "&MB_EXPORT"     'export table
    session.FindById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").SelectContextMenuItem "&PC"
    session.FindById("wnd[1]/usr/subSUBSCREEN_STEPLOOP:SAPLSPO5:0150/sub:SAPLSPO5:0150/radSPOPLI-SELFLAG[4,0]").Select          'select "In the clipboard"
    session.FindById("wnd[1]/tbar[0]/btn[0]").Press
    
    Set wsSAP = Sheets.Add(After:=ws)
    wsSAP.Name = "SAP"
    
    
    Range("A1").Select                                  'copy to Excel and format
    ActiveSheet.Paste
    
'        Selection.TextToColumns Destination:=Range("A1"), DataType:=xlDelimited, _
'        TextQualifier:=xlDoubleQuote, ConsecutiveDelimiter:=False, Tab:=True, _
'        Semicolon:=False, Comma:=False, Space:=False, Other:=True, OtherChar _
'        :="|", FieldInfo:=Array(Array(1, 2), Array(2, 1), Array(3, 1), Array(4, 1), Array(5, _
'        1), Array(6, 1), Array(7, 1), Array(8, 1), Array(9, 1), Array(10, 1), Array(11, 1), Array(12 _
'        , 1), Array(13, 1), Array(14, 1), Array(15, 1), Array(16, 1), Array(17, 1), Array(18, 1), _
'        Array(19, 1), Array(20, 1), Array(21, 1), Array(22, 1), Array(23, 1), Array(24, 1), Array( _
'        25, 1), Array(26, 1), Array(27, 1), Array(28, 1), Array(29, 1), Array(30, 1)), _
'        TrailingMinusNumbers:=True
    Columns("A:A").Select
    Selection.Delete Shift:=xlToLeft
    Rows("1:4").Select
    Selection.Delete Shift:=xlUp
    Rows("2:2").Select
    Selection.Delete Shift:=xlUp
    Range("A1").Select
    Range(Selection, Selection.End(xlToRight)).Select
    Range(Selection, Selection.End(xlDown)).Select
    Application.CutCopyMode = False
    ActiveSheet.ListObjects.Add(xlSrcRange, Range("$A$1:$AB$46"), , xlYes).Name = _
        "TableSAP"
    
    TrimTableHeadersAdvanced wsSAP, "TableSAP"
    
    Range("TableSAP[[#Headers],[Channel Text]]").Select
    Selection.ListObject.ListColumns.Add
    Range("TableSAP[[#Headers],[Column1]]").Select
    Selection.ListObject.ListColumns.Add
    Range("TableSAP[[#Headers],[Column1]]").Select
    ActiveCell.FormulaR1C1 = "Created on"
    Range("TableSAP[[#Headers],[Column2]]").Select
    ActiveCell.FormulaR1C1 = "Invoice status"
    Range("AC2").Select

    Range("AC2").Select

    ActiveCell.FormulaR1C1 = "=XLOOKUP(TRIM(RC[-24]),SmartDOC!C[-28],SmartDOC!C[13])"
    Sheets("SAP").Select
    Range("AD2").Select
    ActiveCell.FormulaR1C1 = "=XLOOKUP(TRIM(RC[-25]),SmartDOC!C[-29],SmartDOC!C[-17])"
    Range("AD3").Select
    
    
End Sub

Sub TrimTableHeadersAdvanced(ByVal TargetWorksheet As Worksheet, _
                            ByVal TableName As String, _
                            Optional ByVal SkipErrors As Boolean = True)
    ' Trims whitespace from all headers in a specified table
    ' Parameters:
    ' - TargetWorksheet: Worksheet containing the table
    ' - TableName: Name of the table to modify
    ' - SkipErrors: True to skip errors (default), False to raise them
    
    On Error GoTo ErrorHandler
    Application.Volatile False
    
    Dim tbl As ListObject
    Dim col As ListColumn
    
    ' Validate inputs
    If TargetWorksheet Is Nothing Then
        Err.Raise Number:=vbObjectError + 1, _
                  Source:="TrimTableHeadersAdvanced", _
                  Description:="Invalid worksheet reference"
    End If
    
    Set tbl = TargetWorksheet.ListObjects(TableName)
    
    ' Loop through columns
    For Each col In tbl.ListColumns
        col.Name = Trim(col.Name)
    Next col

    Exit Sub

ErrorHandler:
    If SkipErrors Then
        Resume Next
    Else
        ' Preserve original error information
        Dim errMsg As String
        errMsg = "Error " & Err.Number & ": " & Err.Description & _
                 " (Source: " & Err.Source & ")"
        Err.Raise Number:=Err.Number, Source:=Err.Source, Description:=errMsg
    End If
End Sub

