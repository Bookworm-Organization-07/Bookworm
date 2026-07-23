#After accepting the invitation 

1)repo clone karna hai

2)go inside repo by 

  cd bookworm

3) Branch check karo Pehle 

  git branch -a

output aisa kych ayega

* main

remotes/origin/main

remotes/origin/develop

4) if develop branch doesn't show or exist do this 

  git checkout -b develop origin/develop

5) sablog apni apni feature branch banayege
ex:-

  git checkout -b feature/product

#GitHub instructions for everyone for Project:

make sure everyone has git installed in their systems and the active github accountt

1) for confirmation 

  git --version 


2) Git Identity set karwana (ek hi baar)

  git config --global user.name "Name"

  git config --global user.email "email@example.com"


3) Repository Clone

  git clone <repo-url>

  repo url mei grp pr dal dunga ya fir tum logo ne invitation aya hoga toh tum bhi dekh saktae ho


4) Har member apni feature branch banayega

  git checkout develop

  git pull origin develop

  git checkout -b feature/auth

  git checkout -b feature/product

  llikewise

5)Daily Workflow (Ye sabse important hai)

Har coding session se pehle:

  git checkout develop

  git pull origin develop

  then

  git checkout feature/auth
  
  git merge develop
  
  then coding part


6) Push

  git add .

  git commit -m "feat: Added User Entity"

  git push origin feature/auth


7. Pull Request

    Feature Branch

      ↓

    Develop

  Main me direct push nahi karna hai.

  basic commands jo sabko use karna ana chahiye 

  git clone

  git checkout
  
  git checkout -b

  git status

  git add .

  git commit -m ""
  
  git pull

  git push
