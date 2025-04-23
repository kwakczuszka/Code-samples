import pytest
from time import sleep
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys

class TestVales001():
  
  def setup_method(self):
    config                          = json.load(open("comidaauto.conf", "r"))

    self.URL                        = config["web"]["url"]
    self.passwd                     = config["web"]["passwd"]
    self.login                      = config["web"]["login"]
    self.listElement                = config["locators"]["listElement"]

    self.driver                     = webdriver.Chrome()  
    self.actions                    = ActionChains(self.driver)
      
  def teardown_method(self):
    self.driver.quit()
  
  def test_vales001(self):
    self.driver.get("https://web.corp.iberia.es/ValesComida/paginas/principal/PRINCIPALFrInicio.html")
    with open('numbers.txt') as my_file:
      num_array = my_file.readlines()
    print("num done")
    self.driver.set_window_size(1552, 936)


    self.driver.implicitly_wait(5)
    
    
    textbox = self.driver.find_element(By.ID, 'username')
    textbox.send_keys(self.login)
    textbox = self.driver.find_element(By.ID, 'password')
    textbox.send_keys(self.passwd)
    buttonLogin = self.driver.find_element(By.NAME, 'Submit')
    buttonLogin.click()
    sleep(1)
    self.driver.switch_to.parent_frame()
    self.driver.switch_to.frame("cabecerarrhh")

    self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[2]/td/table/tbody/tr/td[1]/table/tbody/tr/td[2]/a/img").click()
    self.driver.switch_to.parent_frame()
    
    self.driver.switch_to.frame("contenidorrhh")
    self.driver.switch_to.frame("listado")
    self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td[1]/table/tbody/tr[1]/td[2]").click()
    self.driver.switch_to.parent_frame()
    sleep(1)
    self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td[2]/a[2]/img").click()
    sleep(1)
    self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td[2]/a/img").click()
    for number in num_array:
      self.driver.implicitly_wait(15)
      print(number.strip())
    
      self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[1]/td/table/tbody/tr/td/table[3]/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/input").send_keys(number.strip())
      self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[1]/td/table/tbody/tr/td/table[3]/tbody/tr/td/table/tbody/tr/td/table/tbody/tr[2]/td[2]/input").send_keys(Keys.ENTER)
      sleep(0.5)
      self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td[2]/a[1]/img").click() #guardar
      try:
        self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/div/table/tbody/tr/td/a[2]/img").click()
      except:
        self.driver.implicitly_wait(4)
        self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td/a/img").click()
        self.driver.switch_to.frame("listado")
        self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td[1]/table/tbody/tr[1]/td[2]").click()
        self.driver.switch_to.parent_frame()
        sleep(1)
        self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td[2]/a[2]/img").click()
        sleep(1)
        self.driver.find_element(By.XPATH, "/html/body/table/tbody/tr/td/table/tbody/tr[3]/td/table/tbody/tr/td[3]/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr/td[2]/a/img").click()
    while True:
      pass
                
test = TestVales001()
test.setup_method()
test.test_vales001()