#Ansible can help you quickly deploy the online module. 

1. You need to install Ansible on the control host and python on other controlled machines. 
2. Modify the groups/all file, and replace the host to the corresponding host according to  your need.
3. Configure login from control host to all hosts without password.
4. Deploy the online module using "ansible-playbook -i hosts site.yml".
5. Populate the database.
6. Use the workload generator to test the online module.
