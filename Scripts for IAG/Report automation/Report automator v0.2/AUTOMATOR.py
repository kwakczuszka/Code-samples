from sys import argv, exit as sys_exit
from os import path, startfile
from PyQt5.QtCore import Qt, QTimer, QPropertyAnimation, QEasingCurve
from PyQt5.QtGui import QPixmap, QFont, QIcon 
from PyQt5.QtWidgets import (QApplication, QMainWindow, QWidget, QGridLayout,
                            QPushButton, QSplashScreen, QComboBox, QVBoxLayout,
                            QLabel, QHBoxLayout, QFileDialog, QMessageBox)
from xlwings import App as xlapp
import os
import win32com.client
from pythoncom import CoUninitialize
from pathlib import Path


def resource_path(relative_path):
    """Get absolute path to resource, works for dev and PyInstaller"""
    try:
        # PyInstaller creates a temp folder stored in _MEIPASS
        base_path = _MEIPASS
    except Exception:
        # Use the directory of the script when not bundled
        base_path = path.abspath(path.dirname(__file__))
    
    return path.join(base_path, relative_path)

OPCO_CONFIG = {
    "Iberia": {
        "buttons": [
            {"name": "Banco Report", "handler": "banco_report"},
            {"name": "Foreign Report", "handler": "foreign_report"},
            {"name": "[PLACEHOLDER]GL Analysis", "handler": "gl_analysis"}
        ]
    },
    "Aer Lingus": {
        "buttons": [
            {"name": "[DEVELOPMENT]EI Invoice Summary", "handler": "inv_summary"},
            {"name": "[PLACEHOLDER]Fuel Analysis", "handler": "fuel_analysis"},
            {"name": "[PLACEHOLDER]BA Compliance", "handler": "ba_compliance"}
        ]
    }
}

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Automated reports - IAG GBS")
        self.setGeometry(100, 100, 600, 600)
        self.setWindowIcon(QIcon(resource_path("iagicon.png")))
        
        self.central_widget = QWidget()
        self.setCentralWidget(self.central_widget)
        self.main_layout = QVBoxLayout(self.central_widget)
        
        screen_geometry = QApplication.primaryScreen().availableGeometry()
        x = (screen_geometry.width() - self.width()) // 2
        y = (screen_geometry.height() - self.height()) // 2
        self.move(x, y)
        self.current_opco = None
        
        self.animation = QPropertyAnimation(self, b"windowOpacity")
        self.animation.setEasingCurve(QEasingCurve.InOutQuad)
        
        self.show_opco_selection()

    def show_opco_selection(self):
        self.clear_layout()
        self.current_opco = None
        
        opco_widget = QWidget()
        layout = QVBoxLayout(opco_widget)
        
        # Title
        title = QLabel("Select Operating Company")
        title.setAlignment(Qt.AlignCenter)
        title.setFont(QFont("Arial", 18, QFont.Bold))
        layout.addWidget(title)
        
        # Dropdown
        self.opco_combo = QComboBox()
        self.opco_combo.addItems(OPCO_CONFIG.keys())
        self.opco_combo.setFixedSize(300, 40)
        self.opco_combo.setFont(QFont("Arial", 12))
        layout.addWidget(self.opco_combo, alignment=Qt.AlignCenter)
        
        # Continue Button
        continue_btn = QPushButton("Continue")
        continue_btn.setFixedSize(150, 40)
        continue_btn.clicked.connect(self.load_opco_interface)
        layout.addWidget(continue_btn, alignment=Qt.AlignCenter)
        
        self.main_layout.addWidget(opco_widget)
        self.fade_in()
    
    def fade_in(self):
        splash_path = resource_path("splash2.png")
        # Replace backslashes with forward slashes for Qt (Windows-specific)
        splash_path = splash_path.replace("\\", "/")
        self.setStyleSheet(f"""
            QMainWindow {{
                background-image: url({splash_path});
                background-position: center;
                background-repeat: no-repeat;
            }}
        """)
        self.animation.stop()
        self.animation.setDuration(300)
        self.animation.setStartValue(0.0)
        self.animation.setEndValue(1.0)
        self.animation.start()   
        
    def load_opco_interface(self):
        self.current_opco = self.opco_combo.currentText()
        self.clear_layout()
        
        header = QWidget()
        header_layout = QVBoxLayout(header)  
        button_layout = QHBoxLayout()
        button_layout.setContentsMargins(0, 0, 0, 0)  
        back_btn = QPushButton("← Back")
        back_btn.setFixedSize(80, 30)
        back_btn.clicked.connect(self.show_opco_selection)

        button_layout.addWidget(back_btn, alignment=Qt.AlignLeft)
        header_layout.addLayout(button_layout)  

        title = QLabel(f"{self.current_opco} Reports")
        title.setFont(QFont("Arial", 16, QFont.Bold))
        header_layout.addWidget(title, alignment=Qt.AlignCenter)

        grid_widget = QWidget()
        grid_layout = QGridLayout(grid_widget)
        grid_layout.setContentsMargins(50, 50, 50, 50)
        grid_layout.setSpacing(30)
        
        button_style = """
            QPushButton {
                background-color: #FFFFFF;
                color: black;
                border-radius: 10px;
                padding: 15px;
                font-size: 14px;
            }
            QPushButton:hover { background-color: #F1F1F1; }
            QPushButton:pressed { background-color: #E1E1E1; }
        """

        for idx, btn_config in enumerate(OPCO_CONFIG[self.current_opco]["buttons"]):
            btn = QPushButton(btn_config["name"])
            btn.setFixedSize(250, 120)
            btn.setFont(QFont("Arial", 12))
            btn.setStyleSheet(button_style)
            btn.clicked.connect(lambda _, h=btn_config["handler"]: self.handle_button_click(h))
            row = idx // 2
            col = idx % 2
            grid_layout.addWidget(btn, row, col, alignment=Qt.AlignCenter)
        
        self.main_layout.addWidget(header)
        self.main_layout.addWidget(grid_widget)

#HANDLERS
    def handle_button_click(self, handler_name):
        if handler_name == "banco_report":
            self.run_banco_report()
        elif handler_name == "foreign_report":
            self.run_foreign_report()
        elif handler_name == "inv_summary":
            self.run_ei_inv_summary()

# REPORTS FUNCTIONS
    def run_foreign_report(self):
        try:
            QMessageBox.information(self, "Foreign Report", 
                "Please select directory for foreign report.")
            
            directory_path = QFileDialog.getExistingDirectory(self, 
                "Select Directory for Foreign Report", "")
                    
            if not directory_path:
                return

            with open(resource_path("ForeignReport.vbs"), "r", encoding="utf-8") as f:
                vba_code = f.read()

            xlsm_path = self.create_macro_file(directory_path, vba_code)
            app = xlapp(visible=False)
            wb = app.books.open(xlsm_path)
            
            try:
                wb.macro("ForeignReport")()
                wb.save()
            finally:
                wb.close()
                app.quit()

            QMessageBox.information(self, "Success", "Foreign Report generated successfully!")

        except Exception as e:
            QMessageBox.critical(self, "Error", f"Failed to generate report:\n{str(e)}")
        finally:
            wb.close()
            app.quit()

    def run_banco_report(self):
        try:
            QMessageBox.information(self, "Banco Report", 
                "Please select .xlsx file with FBL1N data in 'Sheet 1' and LFA data in 'LFA' sheet.")
            
            file_path, _ = QFileDialog.getOpenFileName(
                self, "Select Excel File", "", "Excel Files (*.xlsx *.xlsm *.xls)"
            )
            
            if not file_path:
                return
            
            countries_data = []
            with open(resource_path("countries.txt"), 'r') as file:
                for line in file:
                    line = line.strip()
                    if line:  # Skip empty lines
                        parts = line.split(';')
                        if len(parts) == 2:  # Only process valid lines
                            countries_data.append(parts)  # Store as list of lists
            with xlapp(visible=False) as app:
                wb = app.books.open(file_path)
                try:
                    sheet = wb.sheets['Countries']
                    sheet.clear()
                except:
                    sheet = wb.sheets.add(name='Countries')
                
                if countries_data:
                    sheet.range('A1').value = countries_data
                
                wb.save()
                wb.close()

            with open(resource_path("BancoReport.vbs"), "r", encoding="utf-8") as f:
                vba_code = f.read()

            xlsm_path = self.create_macro_file(file_path, vba_code)
            app = xlapp(visible=False)
            wb = app.books.open(xlsm_path)
            
            try:
                wb.macro("BancoReport")()
                wb.save()
            finally:
                wb.close()
                app.quit()

            QMessageBox.information(self, "Success", "Banco Report generated successfully!")

        except Exception as e:
            QMessageBox.critical(self, "Error", f"Failed to generate report:\n{str(e)}")
        finally:
            wb.close()
            app.quit()

    def run_ei_inv_summary(self):
        try:
            QMessageBox.information(self, "EI Invoice Summary", 
                "Please select .xlsx file with SmartDOC data in 'SmartDOC' sheet.")
            
            file_path, _ = QFileDialog.getOpenFileName(
                self, "Select Excel File", "", "Excel Files (*.xlsx)"
            )
            
            if not file_path:
                return
            
            with open(resource_path("InvoiceSummary.vbs"), "r", encoding="utf-8") as f:
                vba_code = f.read()

            xlsm_path = self.create_macro_file(file_path, vba_code)
            app = xlapp(visible=False)
            wb = app.books.open(xlsm_path)
            
            try:
                wb.macro("InvoiceSummary")()
                wb.save()
                
            finally:
                wb.close()
                app.quit()

            QMessageBox.information(self, "Success", "Invoice Summary generated successfully!")

        except Exception as e:
            QMessageBox.critical(self, "Error", f"Failed to generate report:\n{str(e)}")

            
    def create_macro_file(self, xlsx_path, vba_code): 
        excel = None
        wb = None
        ocx_path = r"C:\Program Files (x86)\SAP\FrontEnd\SAPgui\sapfewse.ocx"  # Hardcoded path
        
        try:
            # Resolve and validate paths
            xlsx_path = str(Path(xlsx_path).resolve())
            
            # Determine output path
            if os.path.isdir(xlsx_path):
                xlsm_path = str(Path(xlsx_path) / "generated_report.xlsm")
            else:
                xlsm_path = str(Path(xlsx_path).with_suffix('.xlsm'))

            # Create Excel instance
            excel = win32com.client.Dispatch("Excel.Application")
            excel.Visible = False
            excel.DisplayAlerts = False

            # Create new workbook or open existing
            if os.path.isdir(xlsx_path):
                wb = excel.Workbooks.Add()
            else:
                wb = excel.Workbooks.Open(xlsx_path)

            # Save as XLSM first to enable macros
            wb.SaveAs(xlsm_path, FileFormat=52)  # 52 = xlOpenXMLWorkbookMacroEnabled

            # Add VBA code
            vba_module = wb.VBProject.VBComponents.Add(1)  # Standard module
            vba_module.CodeModule.AddFromString(vba_code)

            # Add OCX reference directly
            wb.VBProject.References.AddFromFile(ocx_path)

            # Final save and close
            wb.Save()
            wb.Close()
            
            return xlsm_path

        except Exception as e:
            raise RuntimeError(f"Failed to create macro file: {str(e)}") from e
        finally:
            # Cleanup sequence
            if wb is not None:
                try: wb.Close(SaveChanges=False)
                except: pass
                del wb
            if excel is not None:
                try: excel.Quit()
                except: pass
                del excel
            CoUninitialize()

    def clear_layout(self):
        while self.main_layout.count():
            child = self.main_layout.takeAt(0)
            if child.widget():
                child.widget().deleteLater()


class App(QApplication):
    def __init__(self, argv):
        super().__init__(argv)
        
        self.splash = QSplashScreen(QPixmap(resource_path("splash.png")))
        self.splash.setWindowFlags(Qt.WindowStaysOnTopHint | Qt.FramelessWindowHint)
        self.splash.show()
        
        QTimer.singleShot(2500, self.initialize_main_window)

    def initialize_main_window(self):
        self.main_window = MainWindow()
        self.main_window.show()
        self.splash.finish(self.main_window)

if __name__ == "__main__":
    app = App(argv)
    sys_exit(app.exec_())