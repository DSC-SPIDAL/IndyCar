import mechanize

url = "http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login"

br = mechanize.Browser()
br.open(f"{url}?action=send")
#br.select_form("_ngcontent-dap-c326")
br.form['token'] = 'Enter your Name'
req = br.submit()

