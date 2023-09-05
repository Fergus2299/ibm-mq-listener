import subprocess

'''
This is a python file which will run the Java for you.

it's purpose is to simulate what Unity will eventually do. 

mvn spring-boot:run
'''

def start_spring_boot_app():
    cmd = ["mvn", "spring-boot:run"]
    subprocess.run(cmd)
    
if __name__ == "__main__":
    start_spring_boot_app()
