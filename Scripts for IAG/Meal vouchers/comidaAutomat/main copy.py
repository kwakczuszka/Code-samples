import pytest
import json
from time import sleep
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.action_chains import ActionChains


class TestVales001:
  def setup_method(self):
    # Load configuration
    config = json.load(open("comidaauto.conf", "r"))

    # Set up config values
    self.URL = config["web"]["url"]
    self.passwd = config["web"]["passwd"]
    self.login = config["web"]["login"]
    self.locators = config["locators"]

    # Initialize WebDriver and ActionChains
    self.driver = webdriver.Chrome()
    self.actions = ActionChains(self.driver)

  def teardown_method(self):
    self.driver.quit()

  def test_vales001(self):
    # Navigate to URL
    self.driver.get(self.URL)
    with open('numbers.txt') as my_file:
        num_array = my_file.readlines()

    self.driver.set_window_size(1552, 936)
    self.driver.implicitly_wait(5)

    # Login to the page
    self.driver.find_element(By.ID, self.locators["usernameField"]).send_keys(self.login)
    self.driver.find_element(By.ID, self.locators["passwordField"]).send_keys(self.passwd)
    self.driver.find_element(By.NAME, self.locators["loginButton"]).click()
    sleep(10)

    # Switch to header frame
    self.driver.switch_to.parent_frame()
    self.driver.switch_to.frame(self.locators["frameHeader"])

    # Navigate to desired section
    self.driver.find_element(By.XPATH, self.locators["buttonNavImage"]).click()
    self.driver.switch_to.parent_frame()
    self.driver.switch_to.frame(self.locators["frameContent"])
    self.driver.switch_to.frame(self.locators["frameList"])
    self.driver.find_element(By.XPATH, self.locators["listElement"]).click()
    self.driver.switch_to.parent_frame()

    # Upload each number in the list
    for number in num_array:
        print(number.strip())
        input_field = self.driver.find_element(By.XPATH, self.locators["inputField"])
        input_field.send_keys(number.strip())
        input_field.send_keys(Keys.ENTER)
        self.driver.find_element(By.XPATH, self.locators["searchEnterButton"]).click()
        try:
            self.driver.find_element(By.CSS_SELECTOR, self.locators["navLink"]).click()
        except:
            self.driver.find_element(By.XPATH, self.locators["defaultNavLinkImage"]).click()
            self.driver.switch_to.frame(self.locators["frameList"])
            self.driver.find_element(By.XPATH, self.locators["listElement"]).click()
            self.driver.switch_to.parent_frame()
    while True:
      pass


test = TestVales001()
test.setup_method()
test.test_vales001()
