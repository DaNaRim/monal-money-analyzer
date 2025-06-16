up:
	docker-compose -p monal-money-analyzer-v0-1-0 up --build

cloc:
    "D:\Program\cloc-1.94.exe" "C:\Users\DaNaRim\IdeaProjects\monal-money-analyzer" --exclude-dir=node_modules,.git,.idea,.mvn,logs,target,build,coverage,node

export:
    docker export monal-money-analyzer-v0-1-0 > monal-money-analyzer-v0-1-0.tar

load:
    docker load < C:\Users\DaNaRim\Desktop\monal-v0-1-0.tar

#build:
#    sudo mvn install -DskipTests -Dcheckstyle.skip -Djacoco.skip -Dskip.npm

up:
        docker-compose up --build



restart:
        sudo systemctl restart monal.service

build:
        sudo mvn install -DskipTests -Dcheckstyle.skip -Djacoco.skip

build-all:
        sudo mvn install -DskipTests -Dcheckstyle.skip -Djacoco.skip -Dskip.npm -Dskip.yarn

log:
        sudo journalctl -n 40 -f -u monal.service