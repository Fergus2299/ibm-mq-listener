import subprocess
import os
'''
This is a python file which will run the Java for you.

it's purpose is to simulate what Unity will eventually do. 

mvn spring-boot:run
'''

def start_spring_boot_app():
    # cmd = ["mvn", "spring-boot:run"]
    ##WINDOWS##
    cmd = ["C:\\apache-maven\\bin\\mvn.cmd", "spring-boot:run"]
    subprocess.run(cmd)

def set_env_variables(**properties):
    """
    Set environment variables from provided properties.
    """
    for key, value in properties.items():
        os.environ[key] = value


    
    
    
def run_with_properties(queue_manager, channel, conn_name, user, password):
    """
    Accepts properties as arguments, sets them as environment variables, 
    and starts the Spring Boot application.
    """
    set_env_variables(
        ibm_mq_queueManager=queue_manager,
        ibm_mq_channel=channel,
        ibm_mq_connName=conn_name,
        ibm_mq_user=user,
        ibm_mq_password=password,
    )
    start_spring_boot_app()    
    
    
if __name__ == "__main__":
        run_with_properties(
        queue_manager="QM1",
        channel="DEV.ADMIN.SVRCONN",
        conn_name="13.87.80.195(1414)",
        user="admin",
        password="passw0rd",
    )
