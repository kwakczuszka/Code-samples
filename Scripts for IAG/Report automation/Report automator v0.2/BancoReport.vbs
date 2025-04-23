Sub BancoReport()
    Dim ws As Worksheet, wsReport As Worksheet, lfaWs As Worksheet, newWb As Workbook
    Dim assignmentCol As Long, accountCol As Long, countryCol As Long, amountCol As Long
    Dim lastRow As Long, i As Long, colIndex As Long, docCurrencyCol As Long
    Dim cellStr As String, formulaStr As String
    Dim dict As Object, srcData As Variant, results As Variant, key As String
    Dim rng As Range, lookupValue As Range, lookupArray As Range, returnArray As Range
    Dim result As Variant, cell As Range
    
    ' Speed up execution
    Application.ScreenUpdating = False
    Application.Calculation = xlCalculationManual
    Application.EnableEvents = False
    Application.DisplayAlerts = False
    
    Set ws = ThisWorkbook.Sheets("Sheet1")
    
    ' --- Assignment Column Processing ---
    assignmentCol = 0
    For i = 1 To ws.UsedRange.Columns.Count
        If ws.Cells(1, i).Value = "Assignment" Then
            assignmentCol = i
            Exit For
        End If
    Next i
    
    If assignmentCol = 0 Then
        MsgBox "Assignment column not found."
        Exit Sub
    End If
    
    lastRow = ws.Cells(ws.Rows.Count, assignmentCol).End(xlUp).Row
    ' Process assignment column
    For i = lastRow To 2 Step -1
        If Trim(ws.Cells(i, assignmentCol).Value) = "" Then
            ws.Rows(i).Delete
        Else
            cellStr = CStr(Val(ws.Cells(i, assignmentCol).Value))
            If Left(cellStr, 1) = "2" Or Left(cellStr, 2) = "46" Or _
               Left(cellStr, 2) = "48" Or Left(cellStr, 2) = "51" Then
                ws.Rows(i).Delete
            End If
        End If
    Next i
    ' --- Account Column Processing ---
    accountCol = 0
    For colIndex = 1 To ws.UsedRange.Columns.Count
        If ws.Cells(1, colIndex).Value = "Account" Then
            accountCol = colIndex
            Exit For
        End If
    Next colIndex
    
    If accountCol = 0 Then
        MsgBox "'Account' column not found."
        Exit Sub
    End If
    ' Insert Country columns
    ws.Columns(accountCol + 1).Resize(, 2).Insert Shift:=xlToRight
    ws.Cells(1, accountCol + 1).Value = "Country"
    ws.Cells(1, accountCol + 2).Value = "Country currency"
    countryCol = accountCol + 1
    
    ' --- Amount Column Formatting ---
    amountCol = 0
    For colIndex = 1 To ws.UsedRange.Columns.Count
        If ws.Cells(1, colIndex).Value = "Amount in doc. curr." Then
            amountCol = colIndex
            Exit For
        End If
    Next colIndex
    If amountCol > 0 Then
        ws.Range(ws.Cells(2, amountCol), ws.Cells(lastRow, amountCol)).NumberFormat = "#,##0.00"
    End If
    
    ' --- Optimized Country Lookup ---
    Set lfaWs = ThisWorkbook.Worksheets("LFA")
    Set dict = CreateObject("Scripting.Dictionary")
    dict.CompareMode = vbTextCompare
    
    ' Load LFA data
    Dim lfaData As Variant
    With lfaWs
        lfaData = .Range("A1:B" & .Cells(.Rows.Count, 1).End(xlUp).Row).Value
    End With
    
    ' Build dictionary
    For i = 2 To UBound(lfaData, 1)
        key = Trim(CStr(lfaData(i, 1)))
        If Not dict.Exists(key) Then
            dict.Add key, lfaData(i, 2)
        End If
    Next i
    
    ' Process country data
    lastRow = ws.Cells(ws.Rows.Count, "P").End(xlUp).Row
    srcData = ws.Range("P2:P" & lastRow).Value
    ReDim results(1 To UBound(srcData), 1 To 1)
    
    For i = 1 To UBound(srcData)
        key = Trim(CStr(srcData(i, 1)))
        results(i, 1) = IIf(dict.Exists(key), dict(key), CVErr(xlErrNA))
    Next i
    
    ' Write country results
    ws.Range("Q2:Q" & lastRow).Value = results
    
    ' --- Post-Processing ---
    ' Delete ES rows
    For i = lastRow To 2 Step -1
    Dim cellValue As Variant
    cellValue = ws.Cells(i, countryCol).Value
    Dim shouldDelete As Boolean
    shouldDelete = False
    
    ' Check for errors first
    If IsError(cellValue) Then
        ' Specific check for #N/A error
        If cellValue = CVErr(xlErrNA) Then
            shouldDelete = True
        End If
    Else
        ' Handle empty strings and "ES" values
        Select Case Trim(CStr(cellValue))
            Case "ES", ""  ' Matches "ES" or empty string (after trim)
                shouldDelete = True
        End Select
    End If
    
    If shouldDelete Then
        ws.Rows(i).Delete
    End If
Next i

' Update last row after deletions
lastRow = ws.Cells(ws.Rows.Count, 1).End(xlUp).Row
    
    ' --- Country Currency Processing ---
    docCurrencyCol = accountCol - 6
    If docCurrencyCol > 0 Then
        With ws.Range(ws.Cells(2, countryCol + 1), ws.Cells(lastRow, countryCol + 1))
            .FormulaR1C1 = "=RC[-1] & RC[" & (docCurrencyCol - (countryCol + 1)) & "]"
            .Value = .Value
        End With
    End If
    
    ' --- Sorting and Subtotals ---
    With ws.Sort
        .SortFields.Clear
        .SortFields.Add2 key:=ws.Range("R:R"), Order:=xlDescending
        .SetRange ws.Range("A1:V" & lastRow)
        .Header = xlYes
        .Apply
    End With
    
    With ws.Range("A1:V" & lastRow)
        .Subtotal GroupBy:=18, Function:=xlSum, TotalList:=Array(9), Replace:=True
    End With
    
    ' --- Report Generation ---
    Set wsReport = Sheets.Add(After:=ws)
    wsReport.Name = "Report"
    
    Set ws = ActiveWorkbook.Worksheets("Sheet1")
    
    Sheets("Sheet1").Select
    Columns("I:I").Select
    ws.Outline.ShowLevels RowLevels:=2
    Selection.SpecialCells(xlCellTypeVisible).Select
    Selection.Copy
    Sheets("Report").Select
    Range("C1").Select
    ActiveSheet.Paste
    Sheets("Sheet1").Select
    Columns("R:R").Select
    ws.Outline.ShowLevels RowLevels:=2
    Selection.SpecialCells(xlCellTypeVisible).Select
    Application.CutCopyMode = False
    Selection.Copy
    Sheets("Report").Select
    Range("A1").Select
    ActiveSheet.Paste
    
    Columns("A:A").Select
    Application.CutCopyMode = False
    Selection.TextToColumns Destination:=Range("A1"), DataType:=xlFixedWidth, _
        FieldInfo:=Array(Array(0, 1), Array(2, 1)), TrailingMinusNumbers:=True
    
     ' Set worksheet
    Set ws = ActiveSheet
    
    ' Find the last non-empty row in column A
    lastRow = wsReport.Cells(ws.Rows.Count, 1).End(xlUp).Row
    wsReport.Rows(lastRow).Delete
    
    ' Define lookup and return ranges (adjust as needed)
    Set lookupArray = Sheets("Countries").Range("A1:A257") ' Lookup column
    Set returnArray = Sheets("Countries").Range("B1:B257") ' Return column

    ' Loop through each non-empty cell in column A
    For Each cell In ws.Range("A2:A" & lastRow)
        If Not IsEmpty(cell.Value) Then
            On Error Resume Next
            result = Application.WorksheetFunction.XLookup(cell.Value, lookupArray, returnArray)
            On Error GoTo 0
            
            If Not IsError(result) Then
                cell.Value = result
            End If
        End If
    Next cell

    Set lookupArray = Nothing
    Set returnArray = Nothing
    
    Range("A1").Select
    ActiveCell.FormulaR1C1 = "Pais"
    Range("B1").Select
    ActiveCell.FormulaR1C1 = "Moneda"
    Range("C1").Select
    ActiveCell.FormulaR1C1 = "Importe"
    Range("A1:C1").Select
    Selection.Style = "Neutral"
    
    Columns("C:C").ColumnWidth = 18
    Columns("B:B").ColumnWidth = 8
    Columns("A:A").ColumnWidth = 16
    
    Range("A1:C500").Select
    Selection.Replace What:="Total", Replacement:="", LookAt:=xlPart, _
        SearchOrder:=xlByColumns, MatchCase:=False, SearchFormat:=False, _
        ReplaceFormat:=False, FormulaVersion:=xlReplaceFormula2
    Selection.Replace What:="-", Replacement:="", LookAt:=xlPart, _
        SearchOrder:=xlByColumns, MatchCase:=False, SearchFormat:=False, _
        ReplaceFormat:=False, FormulaVersion:=xlReplaceFormula2
        
    Worksheets("Report").Copy

    Set newWb = ActiveWorkbook
    newWb.SaveAs Filename:=ThisWorkbook.Path & "\Report.xlsx", FileFormat:=xlOpenXMLWorkbook
    newWb.Close SaveChanges:=False

End Sub

