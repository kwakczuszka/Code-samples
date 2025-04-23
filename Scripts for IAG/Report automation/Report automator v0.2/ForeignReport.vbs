Option Explicit
Public SapGuiAuto
Public objGui As GuiApplication
Public objConn As GuiConnection
Public session As GuiSession
Dim ws As Worksheet
Dim tbl As ListObject
Dim lastRow As Long
Dim i As Long
Dim dictVAT As Object
Dim paymentDateCol As Integer
Dim endDateCol As Integer
Dim paymentStatusCol As Integer

Sub ForeignReport()
    
    Set SapGuiAuto = GetObject("SAPGUI")
    Set objGui = SapGuiAuto.GetScriptingEngine
    Set objConn = objGui.Children(0)
    Set session = objConn.Children(0)
    
    session.findById("wnd[0]").Maximize
    session.StartTransaction ("/OPT/VIM_VA2")
    session.findById("wnd[0]/tbar[1]/btn[17]").Press                                                                'load variant (saved filter set)
    session.findById("wnd[1]/usr/txtV-LOW").Text = "FOREIGN_REPORT"                                                 'variant name
    session.findById("wnd[1]/usr/txtENAME-LOW").Text = ""                                                           'set "Created by" to be empty
    session.findById("wnd[1]").sendVKey 0                                                                           'Enter
    'session.FindById("wnd[0]/usr/btn%_S_XBLNR_%_APP_%-VALU_PUSH").Press                                             'reference multiple selection
    'session.FindById("wnd[1]/usr/tabsTAB_STRIP/tabpNOSV").Select                                                    'exclude values
    'session.FindById("wnd[1]").SendVKey 24                                                                          'Shift+F12  (references to exclude have to be in the clipboard)
    'session.FindById("wnd[1]").SendVKey 8                                                                           'F8 (Execute)

    Dim current_date, first_day_current, first_day_previous, last_day_previous
    Dim date_low, date_high

    current_date = Date
    first_day_current = DateSerial(Year(current_date), Month(current_date), 1)
    first_day_previous = DateAdd("m", -1, first_day_current)
    last_day_previous = DateAdd("d", -1, first_day_current)
    date_low = Right("0" & Day(first_day_previous), 2) & _
            Right("0" & Month(first_day_previous), 2) & _
            Year(first_day_previous)

    date_high = Right("0" & Day(last_day_previous), 2) & _
                Right("0" & Month(last_day_previous), 2) & _
                Year(last_day_previous)

    session.findById("wnd[0]/usr/ctxtS_POSTD-LOW").Text = date_low
    session.findById("wnd[0]/usr/ctxtS_POSTD-HIGH").Text = date_high

    session.findById("wnd[0]/tbar[1]/btn[8]").Press                                                                 'execute search
    
    session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").PressToolbarButton "&MB_VARIANT"       'choose layout
    session.findById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cmbG51_SCREEN-USPEC_LBOX").SetFocus
    session.findById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").ContextMenu      'set filter
    session.findById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").SelectContextMenuItem "&FILTER"
    session.findById("wnd[2]/usr/ssub%_SUBSCREEN_FREESEL:SAPLSSEL:1105/ctxt%%DYN001-LOW").Text = "/KK"        'choose layout /KK/FOREIGN_REPORT
    session.findById("wnd[2]/tbar[0]/btn[0]").Press
    session.findById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").SelectedRows = "0"
    session.findById("wnd[1]/usr/subSUB_CONFIGURATION:SAPLSALV_CUL_LAYOUT_CHOOSE:0500/cntlD500_CONTAINER/shellcont/shell").ClickCurrentCell
    
    session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").PressToolbarContextButton "&MB_EXPORT"     'export table
    session.findById("wnd[0]/usr/cntlCCTRL_MAIN/shellcont/shell/shellcont[0]/shell").SelectContextMenuItem "&PC"
    session.findById("wnd[1]/usr/subSUBSCREEN_STEPLOOP:SAPLSPO5:0150/sub:SAPLSPO5:0150/radSPOPLI-SELFLAG[4,0]").Select          'select "In the clipboard"
    session.findById("wnd[1]/tbar[0]/btn[0]").Press
    
    Range("A1").Select                                  'copy to Excel and format
    ActiveSheet.Paste
        Rows("6:6").Select
    Selection.Delete Shift:=xlUp
    Rows("4:4").Select
    Selection.Delete Shift:=xlUp
    Rows("2:2").Select
    Selection.Delete Shift:=xlUp
    Rows("1:1").Select
    Selection.Delete Shift:=xlUp
    Selection.Delete Shift:=xlUp
    Columns("A:A").Select
    Selection.TextToColumns Destination:=Range("A1"), DataType:=xlDelimited, _
        TextQualifier:=xlDoubleQuote, ConsecutiveDelimiter:=False, Tab:=False, _
        Semicolon:=False, Comma:=False, Space:=False, Other:=True, OtherChar _
        :="|", FieldInfo:=Array(Array(1, 1), Array(2, 1), Array(3, 1), Array(4, 1), Array(5, _
        1), Array(6, 1), Array(7, 1), Array(8, 1), Array(9, 1), Array(10, 1), Array(11, 1)), _
        TrailingMinusNumbers:=True
    Selection.Delete Shift:=xlToLeft
    Columns("E:E").ColumnWidth = 19.78
    Columns("C:C").ColumnWidth = 20.22
    Columns("H:H").ColumnWidth = 27.56
    Columns("G:G").ColumnWidth = 19
    Columns("D:D").ColumnWidth = 19.11
    Selection.ColumnWidth = 26.67
    Selection.ColumnWidth = 44.44

    With ActiveSheet.Range("A1")
        .CurrentRegion.Select
        .Parent.ListObjects.Add 1, .CurrentRegion, , 1
    End With
    
    Set dictVAT = CreateObject("Scripting.Dictionary")
    Set ws = ThisWorkbook.Worksheets("Sheet1")
    Set tbl = ws.ListObjects(1) ' Assumes only one table in sheet
    
    InitializeVATDictionary dictVAT
    
    With tbl
        Dim col As ListColumn
        For Each col In .ListColumns
            col.Name = Trim(col.Name)  ' Removes leading/trailing spaces
        Next col
        .ListColumns.Add(1).Name = "SL no"
        
        .ListColumns("Supplier").Range.Offset(0, 1).EntireColumn.Insert
        .HeaderRowRange.Cells(1, .ListColumns("Supplier").Index + 1).Value = "VAT reg no"
        
        .ListColumns("Paid on").Range.Offset(0, 1).EntireColumn.Insert
        .HeaderRowRange.Cells(1, .ListColumns("Paid on").Index + 1).Value = "End Date of Certificate"
        
        .ListColumns("End Date of Certificate").Range.Offset(0, 1).EntireColumn.Insert
        .HeaderRowRange.Cells(1, .ListColumns("End Date of Certificate").Index + 1).Value = "Payment Status"
        
        paymentDateCol = .ListColumns("Paid on").Index
        endDateCol = .ListColumns("End Date of Certificate").Index
        paymentStatusCol = .ListColumns("Payment Status").Index
        
        lastRow = .DataBodyRange.Rows.Count
        For i = 1 To lastRow
            .DataBodyRange.Cells(i, 1).Value = i
            
            If dictVAT.Exists(.DataBodyRange.Cells(i, .ListColumns("Supplier").Index).Value) Then
                .DataBodyRange.Cells(i, .ListColumns("VAT reg no").Index).Value = _
                    dictVAT(.DataBodyRange.Cells(i, .ListColumns("Supplier").Index).Value)
            End If
            
                .DataBodyRange.Cells(i, endDateCol).Value = DateSerial(2018, 1, 1)
            If Trim(.DataBodyRange.Cells(i, paymentDateCol).Value) <> "" Then
                .DataBodyRange.Cells(i, paymentStatusCol).Value = "Paid"
            Else
                .DataBodyRange.Cells(i, paymentStatusCol).Value = "Not Paid"
            End If
        Next i
    End With
    
    ws.UsedRange.EntireColumn.AutoFit


End Sub

Private Sub InitializeVATDictionary(ByRef dict As Object)
    dict.Add "9000324", "GB848324705"
    dict.Add "9005626", "DE114157527"
    dict.Add "9002809", "DE113835572"
    dict.Add "9001705", "GB840111776"
    dict.Add "9001706", "CA00000000"
    dict.Add "9004174", "NL005467755B01"
    dict.Add "9001709", "CHE353781308"
    dict.Add "9005623", "US00000000"
    dict.Add "9001711", "ATU15156104"
    dict.Add "9004045", "GB208148618"
End Sub






